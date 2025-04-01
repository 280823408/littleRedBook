package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.Note;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.dto.CommentNotice;
import com.example.notes.dto.LikeMessage;
import com.example.notes.dto.NoteCommentDTO;
import com.example.notes.dto.NoteDTO;
import com.example.notes.mapper.NoteCommentMapper;
import com.example.notes.service.INoteCommentService;
import com.example.notes.service.INoteService;
import com.example.notes.utils.MessagesClient;
import com.example.notes.utils.UserCenterClient;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.*;

/**
 * 笔记评论服务实现类
 *
 * <p>功能说明：
 * 1. 实现笔记评论核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存单条评论数据<br>
 * 4. 提供评论查询、新增、删除及点赞功能<br>
 * 5. 事务注解保障数据库操作原子性<br>
 *
 * <p>关键方法：
 * - ID/笔记维度评论查询<br>
 * - 带互斥锁的列表缓存查询<br>
 * - 点赞操作的缓存与数据库双写<br>
 * - 独立事务更新点赞计数器<br>
 *
 * @author Mike
 * @since 2025/3/14
 */
@Service
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements INoteCommentService {
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;
    @Resource
    private MQClient mqClient;
    @Resource
    private UserCenterClient userCenterClient;
    @Resource
    private INoteService noteService;

    /**
     * 根据评论ID查询详细信息
     * @param id 评论唯一标识
     * @return 包含评论实体或错误信息的Result对象
     */
    @Override
    public Result getNoteCommentById(Integer id) {
        try {
            NoteCommentDTO noteCommentDTO = hashRedisClient.hMultiGet(CACHE_COMMENT_KEY + id, NoteCommentDTO.class);
            if (noteCommentDTO != null) {
                return Result.ok(noteCommentDTO);
            }
            NoteComment comment = comment = getById(id);
            if (comment == null) {
                return Result.fail("评论不存在");
            }
            noteCommentDTO = convertToDTO(comment);
            mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_ADD_QUEUE_ROUTING_KEY, noteCommentDTO);
            return Result.ok(noteCommentDTO);
        } catch (ParseException e) {
            return Result.fail("获取评论ID为" + id + "失败");
        }
    }

    /**
     * 获取指定笔记的评论列表
     * @param noteId 笔记唯一标识
     * @return 包含评论集合的Result对象
     */
    @Override
    public Result getNoteCommentsByNoteId(Integer noteId) {
        List<Integer> ids = listObjs(query().getWrapper().eq("note_id", noteId));
        List<NoteCommentDTO> comments = new ArrayList<>();
        for (Integer id : ids) {
            comments.add((NoteCommentDTO) this.getNoteCommentById(id).getData());
        }
        if (comments == null) {
            return Result.fail("获取评论列表失败");
        }
        return Result.ok(comments);
    }

    /**
     * 获取指定用户的评论列表
     * @param userId 用户唯一标识
     * @return 包含评论集合的Result对象
     */
    @Override
    public Result getNoteCommentsByUserId(Integer userId) {
        List<Integer> ids = listObjs(query().getWrapper().eq("user_id", userId));
        List<NoteCommentDTO> comments = new ArrayList<>();
        for (Integer id : ids) {
            comments.add((NoteCommentDTO) this.getNoteCommentById(id).getData());
        }
        if (comments == null) {
            return Result.fail("获取评论列表失败");
        }
        return Result.ok(comments);
    }

    /**
     * 获取用户评论通知
     * @param userId 用户唯一标识
     * @return 包含评论通知的Result对象
     */
    @Override
    public Result getNoteCommentNotice(Integer userId) {
        List<CommentNotice> commentNotices = new ArrayList<>();
        List<NoteDTO> notes = (List<NoteDTO>) noteService.getNotesByUserId(userId).getData();
        for (NoteDTO note : notes) {
            List<NoteCommentDTO> noteComments = (List<NoteCommentDTO>) this.getNoteCommentsByNoteId(note.getId()).getData();
            for (NoteCommentDTO noteComment : noteComments) {
                Object userData = userCenterClient.getUserById(noteComment.getUser().getId()).getData();
                User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
                commentNotices.add(new CommentNotice(
                        noteComment,
                        note,
                        user
                ));
            }
        }
        commentNotices.sort((o1, o2) -> o2.getNoteComment().getCommentTime().compareTo(o1.getNoteComment().getCommentTime()));
        return Result.ok(commentNotices);
    }

    /**
     * 新增评论记录
     * @param noteComment 评论实体对象
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result addNoteComment(NoteComment noteComment) {
        if (!this.save(noteComment)) {
            throw new RuntimeException("添加评论失败");
        }
        NoteCommentDTO noteCommentDTO = convertToDTO(noteComment);
        mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_ADD_QUEUE_ROUTING_KEY, noteCommentDTO);
        return Result.ok();
    }

    /**
     * 删除评论记录
     * @param id 评论唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeNoteComment(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除评论失败");
        }
        mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_DELETE_QUEUE_ROUTING_KEY, id);
        return Result.ok();
    }

    /**
     * 处理用户点赞评论操作
     * @param id 评论唯一标识
     * @param userId 用户唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    public Result likeNoteComment(Integer id, Integer userId) {
        Object likeCommentData =  messagesClient.getLikeCommentByCommentIdAndUserId(
                id, userId).getData();
        LikeComment likeComment = BeanUtil.mapToBean((Map<?, ?>) likeCommentData, LikeComment.class, true);
        boolean isLike = likeComment.getId() != null;
        INoteCommentService noteCommentService = (INoteCommentService) AopContext.currentProxy();
        noteCommentService.updateNoteCommentLikeNum(id, isLike);
        if (isLike) {
            mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_LIKE_QUEUE_ROUTING_KEY, likeComment);
            mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_LIKE_QUEUE_ROUTING_KEY, new LikeMessage(id, -1));
            return Result.ok();
        }
        likeComment.setCommentId(id);
        likeComment.setUserId(userId);
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_LIKE_QUEUE_ROUTING_KEY, likeComment);
        mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_LIKE_QUEUE_ROUTING_KEY, new LikeMessage(id, 1));
        return Result.ok();
    }

    /**
     * 更新评论点赞计数器
     * @param id 评论唯一标识
     * @param isLike 是否取消点赞
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result updateNoteCommentLikeNum(Integer id, boolean isLike) {
        String sql = isLike ? "like_num = like_num - 1" : "like_num = like_num + 1";
        if (!update(new LambdaUpdateWrapper<NoteComment>()
                .eq(NoteComment::getId, id)
                .setSql(sql))) {
            throw new RuntimeException("更新评论点赞数失败");
        }
        return Result.ok();
    }

    /**
     * 从数据库查询指定笔记的评论列表
     * @param noteId 笔记唯一标识
     * @return 按时间倒序排列的评论集合
     */
    private List<NoteCommentDTO> getCommentsByNoteIdFromDB(Integer noteId) {
        List<NoteComment> commentList = query().eq("note_id", noteId)
                .orderByDesc("comment_time")
                .list();
        List<NoteCommentDTO> noteCommentDTOS = new ArrayList<>();
        for (NoteComment comment : commentList) {
            noteCommentDTOS.add(convertToDTO(comment));
        }
        if (noteCommentDTOS.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return noteCommentDTOS;
    }

    /**
     * 将NoteComment转换为NoteCommentDTO，并填充User信息
     * @param comment NoteComment对象
     * @return NoteCommentDTO对象
     */
    private NoteCommentDTO convertToDTO(NoteComment comment) {
        NoteCommentDTO commentDTO = BeanUtil.copyProperties(comment, NoteCommentDTO.class);
        Integer userId = comment.getUserId();
        Object userData = userCenterClient.getUserById(userId).getData();
        User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
        if (user == null) {
            log.error("用户服务调用失败: commentId={" + comment.getId() + "}, userId={" + userId + "}");
        }
        commentDTO.setUser(user);
        return commentDTO;
    }
}
