package com.example.messages.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Message;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.messages.dto.MessageNotice;
import com.example.messages.mapper.MessageMapper;
import com.example.messages.service.IMessageService;
import com.example.messages.utils.UserCenterClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.time.ZoneId;
import java.util.*;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.*;

/**
 * 私信服务实现类
 *
 * <p>功能说明：
 * 1. 实现用户私信核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存单条私信记录<br>
 * 4. 提供私信查询、撤回、批量删除等管理功能<br>
 * 5. 事务注解保障敏感操作原子性<br>
 *
 * <p>关键特性：
 * - 带时效的消息撤回机制<br>
 * - 会话维度的消息列表缓存查询<br>
 * - 双删策略维护缓存一致性<br>
 * - 时间区间批量删除服务<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Service
public class MessageServiceImpl extends ServiceImpl<MessageMapper, Message> implements IMessageService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MQClient mqClient;
    @Resource
    private UserCenterClient userCenterClient;

    /**
     * 根据ID查询私信详情
     * @param id 私信记录唯一标识
     * @return 包含私信实体或错误信息的Result对象
     */
    @Override
    public Result getMessageById(Integer id) {
        try {
            Message message = hashRedisClient.hMultiGet(CACHE_MESSAGE_KEY + id, Message.class);
            if (message == null) {
                message = getById(id);
                if (message == null) {
                    return Result.fail("私信记录不存在");
                }
                mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_ADD_QUEUE_ROUTING_KEY, message);
            }
            return Result.ok(message);
        } catch (ParseException e) {
            return Result.fail("获取私信记录ID为" + id + "失败");
        }
    }

    /**
     * 获取会话消息列表（双向查询）
     * @param receiverId 接收方ID
     * @return 按发送时间倒序排列的私信集合
     */
    @Override
    public Result getMessagesByReceiverIdWithNoRead(Integer receiverId) {
        List<Message> messageList = list(query().getWrapper()
                .eq("receiver_id", receiverId)
                .eq("is_read", 0));
        if (messageList.isEmpty()) {
            return Result.ok(Collections.emptyList());
        }
        return Result.ok(messageList);
    }

    /**
     * 获取会话消息列表（双向查询）
     * @param sendId 发送方ID
     * @param receiverId 接收方ID
     * @return 按发送时间倒序排列的私信集合
     */
    @Override
    public Result getMessagesBySenderIdAndReceiverIdOrderBySendTime(Integer sendId, Integer receiverId) {
        List<Integer> messagesIds = listObjs(query().getWrapper().eq("sender_id", sendId).eq("receiver_id", receiverId).select("id"));
        List<Message> messageList = new ArrayList<>();
        for (Integer id : messagesIds) {
            Result result = this.getMessageById(id);
            if (result.getSuccess()) {
                messageList.add((Message) result.getData());
            }
        }
        return Result.ok(messageList);
//        List<Message> messages = stringRedisClient.queryListWithMutex(
//                CACHE_MESSAGE_SENDERANDRECEIVER_KEY + "%s:%s",
//                Message.class,
//                this::getMessagesFromDB,
//                CACHE_MESSAGE_SENDERANDRECEIVER_TTL,
//                TimeUnit.MINUTES,
//                sendId, receiverId
//        );
//        if (messages == null) {
//            return Result.fail("获取私信列表失败");
//        }
//        return Result.ok(messages);
    }

    /**
     * 获取会话消息列表（双向查询）
     * @param userId 用户ID
     * @return 按发送时间倒序排列的私信集合
     */
    @Override
    public Result getMessagesNotices(Integer userId) {
        List<Message> messages = list(query().getWrapper()
                .eq("receiver_id", userId)
                .eq("is_read", 0));
        List<MessageNotice> messageNotices = new ArrayList<>();
        for (Message message : messages) {
            Object sender = userCenterClient.getUserById(message.getSenderId()).getData();
            User user = BeanUtil.mapToBean((Map<?,?>) sender, User.class, true);
            messageNotices.add(new MessageNotice(user, message, message.getSendTime()));
        }
        messageNotices.sort(((o1, o2) -> o2.getSendTime().compareTo(o1.getSendTime())));
        return Result.ok(messageNotices);
    }

    /**
     * 时效性消息撤回（10分钟内有效）
     * @param message 待撤回私信实体
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result revokeMessageInLimitTime(Message message) {
        if (Math.abs(message.getSendTime().getTime()- System.currentTimeMillis()) > 60 * 10 * 1000) {
            return Result.fail("发出消息时间超出10分钟，不予撤回");
        }
        if (!removeById(message.getId())) {
            throw new RuntimeException("删除私信" + message.getId() + "失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_DELETE_QUEUE_ROUTING_KEY, message.getId());
        return Result.ok();
    }

    /**
     * 删除指定私信记录
     * @param id 私信唯一标识
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result removeMessage(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除私信" + id + "失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_DELETE_QUEUE_ROUTING_KEY, id);
        return Result.ok();
    }

    /**
     * 批量删除私信记录
     * @param ids 私信ID集合
     * @return 操作结果
     */
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

    /**
     * 删除时间区间内的私信记录
     * @param senderId 发送方ID
     * @param receiverId 接收方ID
     * @param startTime 起始时间
     * @param endTime 结束时间
     * @return 操作结果
     */
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

    /**
     * 创建新私信记录
     * @param message 私信实体对象
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result addMessage(Message message) {
        if (!this.save(message)) {
            throw new RuntimeException("添加私信失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_MESSAGE_CACHE_ADD_QUEUE_ROUTING_KEY, message);
        return Result.ok();
    }

    /**
     * 数据库回源查询方法（会话维度）
     * @param sendId 发送方ID
     * @param receiverId 接收方ID
     * @return 双向会话消息列表
     */
    private List<Message> getMessagesFromDB(Integer sendId, Integer receiverId) {
        List<Message> messageList = list(query().getWrapper()
                .and(qw -> qw.eq("sender_id", sendId).eq("receiver_id", receiverId))
                .or(qw -> qw.eq("sender_id", receiverId).eq("receiver_id", sendId))
                .orderByDesc("send_time"));
        if (messageList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return messageList;
    }
}
