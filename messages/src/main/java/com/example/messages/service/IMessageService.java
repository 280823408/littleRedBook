package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.Message;

import java.sql.Timestamp;
import java.util.List;

public interface IMessageService extends IService<Message> {
    Result getMessageById(Integer id);
    Result getMessagesBySenderIdAndReceiverIdOrderBySendTime(Integer sendId, Integer receiverId);
    Result revokeMessageInLimitTime(Message message);
    Result removeMessage(Integer id);
    Result removeMessages(List<Integer> ids);
    Result removeMessagesInTimeInterval(Integer senderId, Integer receiverId, Timestamp startTime, Timestamp endTime);
    Result addMessage(Message message);
}
