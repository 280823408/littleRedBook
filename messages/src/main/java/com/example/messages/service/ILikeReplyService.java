package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;

public interface ILikeReplyService extends IService<LikeReply> {
    Result getLikeReplyById(Integer id);
    Result getLikeRepliesByReplyId(Integer replyId);
    Result getLikeReplyByReplyIdAndUserId(Integer replyId, Integer userId);
    Result removeLikeReply(Integer id);
    Result addLikeReply(LikeReply likeReply);
}
