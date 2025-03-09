package com.example.messages.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Message;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.messages.mapper.MessageMapper;
import com.example.messages.service.IMessageService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.Date;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Override
    public Result getMessageById(Integer id) {
        try {
            Message message = hashRedisClient.hMultiGet(CACHE_MESSAGE_KEY + id, Message.class);
            if (message == null) {
                message = getById(id);
                if (message == null) {
                    return Result.fail("私信记录不存在");
                }
                hashRedisClient.hMultiSet(CACHE_MESSAGE_KEY + id, message);
                hashRedisClient.expire(CACHE_MESSAGE_KEY + id, CACHE_MESSAGE_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(message);
        } catch (ParseException e) {
            return Result.fail("获取私信记录ID为" + id + "失败");
        }
    }

    @Override
    public Result getMessagesBySenderIdAndReceiverIdOrderBySendTime(Integer sendId, Integer receiverId) {
        List<Message> messages = stringRedisClient.queryListWithMutex(
                CACHE_MESSAGE_SENDERANDRECEIVER_KEY + "%s:%s",
                Message.class,
                this::getMessagesFromDB,
                CACHE_MESSAGE_SENDERANDRECEIVER_TTL,
                TimeUnit.MINUTES,
                sendId, receiverId

        );
        if (messages == null) {
            return Result.fail("获取私信列表失败");
        }
        return Result.ok(messages);
    }

    @Override
    @Transactional
    public Result revokeMessageInLimitTime(Message message) {
        if (Math.abs(message.getSendTime().getTime()- System.currentTimeMillis()) > 60 * 10 * 1000) {
            return Result.fail("发出消息时间超出10分钟，不予撤回");
        }
        if (!removeById(message.getId())) {
            return Result.fail("删除私信" + message.getId() + "失败");
        }
        hashRedisClient.delete(CACHE_MESSAGE_KEY + message.getId());
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeMessage(Integer id) {
        if (!this.removeById(id)) {
            return Result.fail("删除私信" + id + "失败");
        }
        hashRedisClient.delete(CACHE_MESSAGE_KEY + id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeMessages(List<Integer> ids) {
        List<Message> messageList = this.list(query().getWrapper().in("id", ids));
        for (Message message: messageList) {
            hashRedisClient.delete(CACHE_MESSAGE_KEY + message.getId());
            this.removeById(message.getId());
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeMessagesInTimeInterval(Integer senderId, Integer receiverId,Timestamp startTime, Timestamp endTime) {
        List<Message> messageList = list(query().getWrapper()
                .ge("send_time", startTime)
                .le("send_time", endTime));
        for (Message message: messageList) {
            this.removeMessage(message.getId());
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addMessage(Message message) {
        if (!this.save(message)) return Result.fail("添加私信失败");
        hashRedisClient.hMultiSet(CACHE_MESSAGE_KEY + message.getId(), message);
        return Result.ok();
    }

    private List<Message> getMessagesFromDB(Integer sendId, Integer receiverId) {
        List<Message> messageList = list(query().getWrapper().and(qw -> qw.eq("sender_id", sendId).eq("receiver_id", receiverId))
                .or(qw -> qw.eq("sender_id", receiverId).eq("receiver_id", sendId))
                .orderByDesc("send_time"));
        if (messageList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return messageList;
    }
}
