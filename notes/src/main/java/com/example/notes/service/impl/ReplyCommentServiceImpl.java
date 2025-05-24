package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.entity.ReplyComment;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.dto.LikeMessage;
import com.example.notes.dto.NoteCommentDTO;
import com.example.notes.dto.ReplyCommentDTO;
import com.example.notes.dto.ReplyNotice;
import com.example.notes.mapper.ReplyCommentMapper;
import com.example.notes.service.INoteCommentService;
import com.example.notes.service.IReplyCommentService;
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
 * 回复评论服务实现类
 *
 * <p>功能说明：
 * 1. 实现评论回复关系核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存单条回复记录<br>
 * 4. 提供回复查询、新增、删除及点赞功能<br>
 * 5. 事务注解保障数据库操作原子性<br>
 *
 * <p>关键方法：
 * - ID/评论维度回复查询<br>
 * - 带互斥锁的列表缓存查询<br>
 * - 点赞操作的缓存与数据库双写<br>
 * - 独立事务更新点赞计数器<br>
 *
 * @author Mike
 * @since 2025/3/14
 */
@Service
public class ReplyCommentServiceImpl extends ServiceImpl<ReplyCommentMapper, ReplyComment> implements IReplyCommentService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;
    @Resource
    private MQClient mqClient;
    @Resource
    private UserCenterClient userCenterClient;
    @Resource
    private INoteCommentService noteCommentService;

    /**
     * 根据ID查询回复评论详情
     * @param id 回复记录唯一标识
     * @return 包含回复实体或错误信息的Result对象
     */
    @Override
    public Result getReplyCommentById(Integer id) {
        try {
            ReplyCommentDTO replyCommentDTO = hashRedisClient.hMultiGet(CACHE_REPLYCOMMENT_KEY + id, ReplyCommentDTO.class);
            if (replyCommentDTO != null) {
                return Result.ok(replyCommentDTO);
            }
            ReplyComment replyComment = getById(id);
            if (replyComment == null) {
                return Result.fail("回复评论不存在");
            }
            replyCommentDTO = convertToDTO(replyComment);
            mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_ADD_QUEUE_ROUTING_KEY, replyCommentDTO);
            return Result.ok(replyCommentDTO);
        } catch (ParseException e) {
            return Result.fail("获取回复评论ID为" + id + "失败");
        }
    }

    /**
     * 获取指定评论的回复列表
     * @param commentId 父评论唯一标识
     * @return 包含回复集合的Result对象
     */
    @Override
    public Result getReplyCommentsByCommentId(Integer commentId) {
        List<Integer> ids = listObjs(query().getWrapper().eq("comment_id", commentId).select("id"));
        List<ReplyCommentDTO> replyComments = new ArrayList<>();
        for (Integer id : ids) {
            Result result = this.getReplyCommentById(id);
            if (result.getSuccess()) {
                replyComments.add((ReplyCommentDTO) result.getData());
            }
        }
        return Result.ok(replyComments);
//        List<ReplyCommentDTO> replyComments = stringRedisClient.queryListWithMutex(
//                CACHE_REPLYCOMMENT_COMMENT_KEY,
//                commentId,
//                ReplyCommentDTO.class,
//                this::getReplyCommentsByCommentIdFromDB,
//                CACHE_REPLYCOMMENT_COMMENT_TTL,
//                TimeUnit.MINUTES
//        );
//        if (replyComments == null) {
//            return Result.fail("获取回复评论列表失败");
//        }
//        return Result.ok(replyComments);
    }

    @Override
    public Result getReplyCommentNotice(Integer userId) {
        List<ReplyNotice> replyNotices = new ArrayList<>();
        List<NoteCommentDTO> noteComments = (List<NoteCommentDTO>) noteCommentService.getNoteCommentsByUserId(userId).getData();
        for (NoteCommentDTO noteComment : noteComments) {
            List<ReplyCommentDTO> replyComments = (List<ReplyCommentDTO>) this.getReplyCommentsByCommentId(noteComment.getId()).getData();
            for (ReplyCommentDTO replyComment : replyComments) {
                Object userData = userCenterClient.getUserById(replyComment.getUser().getId()).getData();
                User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
                replyNotices.add(new ReplyNotice(
                        replyComment,
                        noteComment,
                        user
                ));
            }
        }
        replyNotices.sort((o1, o2) -> o2.getReplyComment().getReplyTime().compareTo(o1.getReplyComment().getReplyTime()));
        return Result.ok(replyNotices);
    }

    /**
     * 新增回复评论记录
     * @param replyComment 回复实体对象
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result addReplyComment(ReplyComment replyComment) {
        if (!this.save(replyComment)) {
            throw new RuntimeException("添加回复评论失败");
        }
        ReplyCommentDTO replyCommentDTO = convertToDTO(replyComment);
        mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_ADD_QUEUE_ROUTING_KEY, replyCommentDTO);
        return Result.ok();
    }

    /**
     * 删除回复评论记录
     * @param id 回复记录唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeReplyComment(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除回复评论失败");
        }
        mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_DELETE_QUEUE_ROUTING_KEY, id);
        return Result.ok();
    }

    /**
     * 处理用户点赞回复操作
     * @param id 回复记录唯一标识
     * @param userId 用户唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    public Result likeReplyComment(Integer id, Integer userId) {
        Object likeReplyData =  messagesClient.getLikeReplyByReplyIdAndUserId(
                id, userId).getData();
        LikeReply likeReply = BeanUtil.mapToBean((Map<?, ?>) likeReplyData, LikeReply.class, true);
        boolean isLike = likeReply.getId() != null;
        IReplyCommentService replyCommentService = (IReplyCommentService) AopContext.currentProxy();
        replyCommentService.updateReplyCommentLikeNum(id, isLike);
        if (isLike) {
            mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_LIKE_QUEUE_ROUTING_KEY, likeReply);
            mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_LIKE_QUEUE_ROUTING_KEY, new LikeMessage(id, -1));
            return Result.ok();
        }
        likeReply.setReplyId(id);
        likeReply.setUserId(userId);
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_LIKE_QUEUE_ROUTING_KEY, likeReply);
        mqClient.sendMessage(TOPIC_NOTES_EXCHANGE, TOPIC_NOTES_EXCHANGE_WITH_NOTES_REPLY_CACHE_LIKE_QUEUE_ROUTING_KEY, new LikeMessage(id, 1));
        return Result.ok();
    }

    /**
     * 更新回复点赞计数器
     * @param id 回复记录唯一标识
     * @param isLike 是否取消点赞
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result updateReplyCommentLikeNum(Integer id, boolean isLike) {
        String sql = isLike ? "like_num = like_num - 1" : "like_num = like_num + 1";
        if (!update(new LambdaUpdateWrapper<ReplyComment>()
                .eq(ReplyComment::getId, id)
                .setSql(sql))) {
            throw new RuntimeException("更新回复点赞数失败");
        }
        return Result.ok();
    }

    /**
     * 从数据库查询评论回复列表
     * @param commentId 父评论唯一标识
     * @return 按时间倒序排列的回复集合
     */
    private List<ReplyCommentDTO> getReplyCommentsByCommentIdFromDB(Integer commentId) {
        List<ReplyComment> replyCommentList = query().eq("comment_id", commentId)
                .orderByDesc("reply_time")
                .list();
        List<ReplyCommentDTO> replyCommentDTOS = new ArrayList<>();
        for (ReplyComment replyComment : replyCommentList) {
            replyCommentDTOS.add(convertToDTO(replyComment));
        }
        if (replyCommentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return replyCommentDTOS;
    }


    private ReplyCommentDTO convertToDTO(ReplyComment replyComment) {
        ReplyCommentDTO replyCommentDTO = BeanUtil.copyProperties(replyComment, ReplyCommentDTO.class);
        Integer userId = replyComment.getUserId();
        Object userData = userCenterClient.getUserById(userId).getData();
        User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
        if (user == null) {
            log.error("用户服务调用失败: ReplycommentId={" + replyComment.getId() + "}, userId={" + userId + "}");
        }
        replyCommentDTO.setUser(user);
        return replyCommentDTO;
    }
}
