package com.example.messages.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.messages.mapper.LikeReplyMapper;
import com.example.messages.service.ILikeReplyService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class LikeReplyServiceImpl extends ServiceImpl<LikeReplyMapper, LikeReply> implements ILikeReplyService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Override
    public Result getLikeReplyById(Integer id) {
        try {
            LikeReply likeReply = hashRedisClient.hMultiGet(CACHE_LIKEREPLY_KEY + id, LikeReply.class);
            if (likeReply == null) {
                likeReply = getById(id);
                if (likeReply == null) {
                    return Result.fail("点赞回复记录不存在");
                }
                hashRedisClient.hMultiSet(CACHE_LIKEREPLY_KEY + id, likeReply);
                hashRedisClient.expire(CACHE_LIKEREPLY_KEY + id, CACHE_LIKEREPLY_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeReply);
        } catch (ParseException e) {
            return Result.fail("获取点赞回复记录ID为" + id + "失败");
        }
    }

    @Override
    public Result getLikeRepliesByReplyId(Integer replyId) {
        List<LikeReply> likeReplyList = stringRedisClient.queryListWithMutex(
                CACHE_LIKEREPLY_REPLY_KEY,
                replyId,
                LikeReply.class,
                this::getLikeRepliesFromDBForReplyId,
                CACHE_LIKEREPLY_REPLY_TTL,
                TimeUnit.MINUTES
        );
        if (likeReplyList == null) {
            return Result.fail("获取点赞回复记录列表失败");
        }
        return Result.ok(likeReplyList);
    }

    @Override
    @Transactional
    public Result removeLikeReply(Integer id) {
        if (!removeById(id)) {
            return Result.fail("删除点赞回复记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKEREPLY_KEY + id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addLikeReply(LikeReply likeReply) {
        if (!save(likeReply)) {
            return Result.fail("添加新的点赞回复记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_LIKEREPLY_KEY + likeReply.getId(), likeReply);
        return Result.ok();
    }

    private List<LikeReply> getLikeRepliesFromDBForReplyId(Integer replyId) {
        List<LikeReply> likeReplyList = list(new QueryWrapper<LikeReply>().eq("reply_id", replyId));
        if (likeReplyList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return likeReplyList;
    }
}
