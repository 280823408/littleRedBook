package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;

public interface ILikeCommentService extends IService<LikeComment> {
    Result getLikeCommentById(Integer id);
    Result getLikeCommentsByCommentId(Integer commentId);
    Result getLikeCommentByCommentIdAndUserId(Integer commentId, Integer userId);
    Result removeLikeComment(Integer id);
    Result addLikeComment(LikeComment likeComment);
}
