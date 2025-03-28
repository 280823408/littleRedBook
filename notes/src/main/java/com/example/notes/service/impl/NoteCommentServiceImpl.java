package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.dto.LikeMessage;
import com.example.notes.mapper.NoteCommentMapper;
import com.example.notes.service.INoteCommentService;
import com.example.notes.utils.MessagesClient;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
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
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;
    @Resource
    private MQClient mqClient;

    /**
     * 根据评论ID查询详细信息
     * @param id 评论唯一标识
     * @return 包含评论实体或错误信息的Result对象
     */
    @Override
    public Result getNoteCommentById(Integer id) {
        try {
            NoteComment comment = hashRedisClient.hMultiGet(CACHE_COMMENT_KEY + id, NoteComment.class);
            if (comment == null) {
                comment = getById(id);
                if (comment == null) {
                    return Result.fail("评论不存在");
                }
                hashRedisClient.hMultiSet(CACHE_COMMENT_KEY + id, comment);
                hashRedisClient.expire(CACHE_COMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(comment);
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
        List<NoteComment> comments = stringRedisClient.queryListWithMutex(
                CACHE_COMMENT_NOTE_KEY,
                noteId,
                NoteComment.class,
                this::getCommentsByNoteIdFromDB,
                CACHE_COMMENT_NOTE_TTL,
                TimeUnit.MINUTES
        );
        if (comments == null) {
            return Result.fail("获取评论列表失败");
        }
        return Result.ok(comments);
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
        hashRedisClient.hMultiSet(CACHE_COMMENT_KEY + noteComment.getId(), noteComment);
        hashRedisClient.expire(CACHE_COMMENT_KEY + noteComment.getId(), CACHE_COMMENT_TTL, TimeUnit.MINUTES);
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
        hashRedisClient.delete(CACHE_COMMENT_KEY + id);
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
            mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_QUEUE_ROUTING_KEY, likeComment);
            mqClient.sendDelayMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_QUEUE_ROUTING_KEY, new LikeMessage(id, -1), 100);
            return Result.ok();
        }
        likeComment.setCommentId(id);
        likeComment.setUserId(userId);
        mqClient.sendMessage("messages.topic", "like.comment", likeComment);
        mqClient.sendDelayMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_COMMENT_CACHE_QUEUE_ROUTING_KEY, new LikeMessage(id, 1), 100);
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
    private List<NoteComment> getCommentsByNoteIdFromDB(Integer noteId) {
        List<NoteComment> commentList = query().eq("note_id", noteId)
                .orderByDesc("comment_time")
                .list();
        if (commentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return commentList;
    }
}
