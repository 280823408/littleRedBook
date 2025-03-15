package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.entity.ReplyComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.mapper.ReplyCommentMapper;
import com.example.notes.service.IReplyCommentService;
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

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class ReplyCommentServiceImpl extends ServiceImpl<ReplyCommentMapper, ReplyComment> implements IReplyCommentService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;

    @Override
    public Result getReplyCommentById(Integer id) {
        try {
            ReplyComment replyComment = hashRedisClient.hMultiGet(CACHE_REPLYCOMMENT_KEY + id, ReplyComment.class);
            if (replyComment == null) {
                replyComment = getById(id);
                if (replyComment == null) {
                    return Result.fail("回复评论不存在");
                }
                hashRedisClient.hMultiSet(CACHE_REPLYCOMMENT_KEY + id, replyComment);
                hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(replyComment);
        } catch (ParseException e) {
            return Result.fail("获取回复评论ID为" + id + "失败");
        }
    }

    @Override
    public Result getReplyCommentsByCommentId(Integer commentId) {
        List<ReplyComment> replyComments = stringRedisClient.queryListWithMutex(
                CACHE_REPLYCOMMENT_COMMENT_KEY,
                commentId,
                ReplyComment.class,
                this::getReplyCommentsByCommentIdFromDB,
                CACHE_REPLYCOMMENT_COMMENT_TTL,
                TimeUnit.MINUTES
        );
        if (replyComments == null) {
            return Result.fail("获取回复评论列表失败");
        }
        return Result.ok(replyComments);
    }

    @Override
    @Transactional
    public Result addReplyComment(ReplyComment replyComment) {
        if (!this.save(replyComment)) {
            throw new RuntimeException("添加回复评论失败");
        }
        hashRedisClient.hMultiSet(CACHE_REPLYCOMMENT_KEY + replyComment.getId(), replyComment);
        hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + replyComment.getId(), CACHE_REPLYCOMMENT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeReplyComment(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除回复评论失败");
        }
        hashRedisClient.delete(CACHE_COMMENT_KEY + id);
        return Result.ok();
    }

    @Override
    public Result likeReplyComment(Integer id, Integer userId) {
        Object likeReplyData =  messagesClient.getLikeReplyByReplyIdAndUserId(
                id, userId).getData();
        LikeReply likeReply = BeanUtil.mapToBean((Map<?, ?>) likeReplyData, LikeReply.class, true);
        boolean isLike = likeReply.getId() != null;
        IReplyCommentService replyCommentService = (IReplyCommentService) AopContext.currentProxy();
        replyCommentService.updateReplyCommentLikeNum(id, isLike);
        if (isLike) {
            messagesClient.removeLikeReply(likeReply.getId());
            hashRedisClient.hIncrement(CACHE_REPLYCOMMENT_KEY + id, "likeNum", -1);
            hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + id, CACHE_REPLYCOMMENT_TTL, TimeUnit.MINUTES);
            return Result.ok();
        }
        likeReply.setReplyId(id);
        likeReply.setUserId(userId);
        messagesClient.addLikeReply(likeReply);
        hashRedisClient.hIncrement(CACHE_REPLYCOMMENT_KEY + id, "likeNum", 1);
        hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + id, CACHE_REPLYCOMMENT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

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
     * 从数据库查询指定评论的回复评论列表（按时间倒序）
     */
    private List<ReplyComment> getReplyCommentsByCommentIdFromDB(Integer commentId) {
        List<ReplyComment> replyCommentList = query().eq("comment_id", commentId)
                .orderByDesc("reply_time")
                .list();
        if (replyCommentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return replyCommentList;
    }
}
