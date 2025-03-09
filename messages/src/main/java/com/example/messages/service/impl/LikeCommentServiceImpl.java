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

    @Override
    @Transactional
    public Result removeLikeComment(Integer id) {
        if (!removeById(id)) {
            return Result.fail("删除点赞评论记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKECOMMENT_KEY + id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addLikeComment(LikeComment likeComment) {
        if (!save(likeComment)) {
            return Result.fail("添加新的点赞评论记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_LIKECOMMENT_KEY + likeComment.getId(), likeComment);
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
