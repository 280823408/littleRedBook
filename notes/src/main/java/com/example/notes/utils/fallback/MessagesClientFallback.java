package com.example.notes.utils.fallback;


import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.entity.LikeReply;
import com.example.notes.utils.MessagesClient;

public class MessagesClientFallback implements MessagesClient {
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

    @Override
    public Result getLikeNoteByNoteIdAndUserId(Integer noteId, Integer userId) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result removeLikeNote(Integer id) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result addLikeNote(LikeNote likeNote) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result getLikeReplyByReplyIdAndUserId(Integer replyId, Integer userId) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result removeLikeReply(Integer id) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result addLikeReply(LikeReply likeReply) {
        return Result.fail("消息服务不可用");
    }
}
