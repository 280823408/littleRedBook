package com.example.notes.utils;


import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;

public class LikeCommentClientFallback implements LikeCommentClient{
    @Override
    public Result addLikeComment(LikeComment likeComment) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result getLikeCommentByCommentIdAndUserId(Integer commentId, Integer userId) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result removeLikeComment(Integer id) {
        return Result.fail("消息服务不可用");
    }
}
