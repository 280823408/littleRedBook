package com.example.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.ReplyComment;

public interface IReplyCommentService extends IService<ReplyComment> {
    Result getReplyCommentById(Integer id);
    Result getReplyCommentsByCommentId(Integer commentId);
    Result addReplyComment(ReplyComment replyComment);
    Result removeReplyComment(Integer id);
    Result likeReplyComment(Integer id, Integer userId);
    Result updateReplyCommentLikeNum(Integer id, boolean isLike);
}
