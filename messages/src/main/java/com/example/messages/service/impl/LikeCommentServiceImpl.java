package com.example.messages.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Concern;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.messages.mapper.LikeCommentMapper;
import com.example.messages.service.ILikeCommentService;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class LikeCommentServiceImpl extends ServiceImpl<LikeCommentMapper, LikeComment> implements ILikeCommentService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Override
    public Result getLikeCommentById(Integer id) {
        try {
            LikeComment likeComment = hashRedisClient.hMultiGet(CACHE_LIKECOMMENT_KEY + id, LikeComment.class);
            if (likeComment == null) {
                likeComment = getById(id);
            }
            if (likeComment == null) {
                return Result.fail("该点赞评论记录ID不存在");
            }
            hashRedisClient.hMultiSet(CACHE_LIKECOMMENT_KEY + id, LikeComment.class);
            hashRedisClient.expire(CACHE_LIKECOMMENT_KEY + id, CACHE_LIKECOMMENT_TTL, TimeUnit.MINUTES);
            return Result.ok(likeComment);
        } catch (ParseException e) {
            return Result.fail("获取点赞评论记录ID为" + id + "失败");
        }
    }

    @Override
    public Result getLikeCommentsByCommentId(Integer commentId) {
        List<LikeComment> likeCommentList = stringRedisClient.queryListWithMutex(
                CACHE_LIKECOMMENT_COMMENT_KEY,
                commentId,
                LikeComment.class,
                this::getLikeCommentsFromDBForCommentId,
                CACHE_LIKECOMMENT_COMMENT_TTL,
                TimeUnit.MINUTES
        );
        if (likeCommentList == null) {
            return Result.fail("获取点赞记录列表失败");
        }
        return Result.ok(likeCommentList);
    }

    // TODO 可以在Redis中设计LikeComment的二级索引，即通过commentId+userId -> id的映射关系，从而达到使用Redis优化查询的目的
    // TODO 本方法未处理缓存穿透和缓存击穿（待后续优化）
    @Override
    public Result getLikeCommentByCommentIdAndUserId(Integer commentId, Integer userId) {
        String key = CACHE_LIKECOMMENT_COMMENT_USER_KEY + commentId + ":" + userId;
        try{
            Integer id = hashRedisClient.hMultiGet(key, Integer.class);
            LikeComment likeComment = null;
            if (id != null) {
                likeComment = (LikeComment) this.getLikeCommentById(id).getData();
            }
            if (likeComment == null) {
                likeComment = query().select("id").eq("comment_id", commentId)
                        .eq("user_id", userId).one();
            }
            if (likeComment == null) {
                return Result.fail("该点赞评论记录不存在");
            }
            if (id == null) {
                hashRedisClient.hMultiSet(key, likeComment.getId());
                hashRedisClient.expire(key,
                        CACHE_LIKECOMMENT_COMMENT_USER_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeComment);
        } catch (ParseException e) {
            return Result.fail("获取点赞记录失败");
        }
    }

    @Override
    @Transactional
    public Result removeLikeComment(Integer id) {
        LikeComment likeComment = (LikeComment) this.getLikeCommentById(id).getData();
        if (!this.removeById(id)) {
            throw new RuntimeException("删除点赞评论记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKECOMMENT_KEY + id);
        hashRedisClient.delete(CACHE_LIKECOMMENT_COMMENT_USER_KEY + likeComment.getCommentId() + ":" + likeComment.getUserId());
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addLikeComment(LikeComment likeComment) {
        if (!this.save(likeComment)) {
            throw new RuntimeException("添加新的点赞评论记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_LIKECOMMENT_KEY + likeComment.getId(), likeComment);
        hashRedisClient.expire(CACHE_LIKECOMMENT_KEY + likeComment.getId(), CACHE_LIKECOMMENT_TTL, TimeUnit.MINUTES);
        hashRedisClient.hMultiSet(CACHE_LIKECOMMENT_COMMENT_USER_KEY + likeComment.getCommentId() + ":" + likeComment.getUserId()
                , likeComment.getId());
        hashRedisClient.expire(CACHE_LIKECOMMENT_COMMENT_USER_KEY + likeComment.getCommentId() + ":" + likeComment.getUserId(),
                CACHE_LIKECOMMENT_COMMENT_USER_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    private List<LikeComment> getLikeCommentsFromDBForCommentId(Integer commentId) {
        List<LikeComment> likeCommentList = list(new QueryWrapper<LikeComment>().eq("comment_id", commentId));
        if (likeCommentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return likeCommentList;
    }
}
