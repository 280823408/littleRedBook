package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.Message;

import java.sql.Timestamp;
import java.util.List;

/**
 * 消息服务接口
 *
 * <p>功能说明：
 * 1. 定义用户消息交互核心业务逻辑接口<br>
 * 2. 实现消息的发送、撤回、删除及会话管理功能<br>
 * 3. 支持单聊消息管理、消息记录查询、批量删除等场景<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单条消息详情<br>
 * - 获取用户间的完整会话记录<br>
 * - 消息限时撤回功能（如5分钟内可撤回）<br>
 * - 单条/批量消息删除操作<br>
 * - 按时间范围清理会话消息<br>
 * - 新增消息记录<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
public interface IMessageService extends IService<Message> {
    /**
     * 根据消息ID查询详细信息
     * @param id 消息唯一标识
     * @return 包含消息实体或错误信息的Result对象
     */
    Result getMessageById(Integer id);

    /**
     * 根据接收方ID获取未读消息列表
     * @param receiverId 接受方用户ID
     * @return 包含消息时间轴列表的Result对象
     */
    Result getMessagesByReceiverIdWithNoRead(Integer receiverId);

    /**
     * 获取指定用户的完整会话记录（按发送时间排序）
     * @param sendId 发送方用户ID
     * @param receiverId 接收方用户ID
     * @return 包含消息时间轴列表的Result对象
     */
    Result getMessagesBySenderIdAndReceiverIdOrderBySendTime(Integer sendId, Integer receiverId);

    /**
     * 获取指定用户的未读消息通知
     * @param userId 用户ID
     * @return 包含消息时间轴列表的Result对象
     */
    Result getMessagesNotices(Integer userId);

    /**
     * 在有效期内撤回消息（如发送后5分钟内）
     * @param message 需要撤回的消息实体
     * @return 包含撤回操作结果的Result对象
     */
    Result revokeMessageInLimitTime(Message message);

    /**
     * 删除单条消息记录
     * @param id 消息唯一标识
     * @return 包含删除操作结果的Result对象
     */
    Result removeMessage(Integer id);

    /**
     * 批量删除消息记录
     * @param ids 消息ID集合
     * @return 包含批量删除结果的Result对象
     */
    Result removeMessages(List<Integer> ids);

    /**
     * 删除指定时间区间内的会话消息
     * @param senderId 发送方ID
     * @param receiverId 接收方ID
     * @param startTime 时间区间起点（包含）
     * @param endTime 时间区间终点（不包含）
     * @return 包含删除数量的Result对象
     */
    Result removeMessagesInTimeInterval(Integer senderId, Integer receiverId, Timestamp startTime, Timestamp endTime);

    /**
     * 新增消息记录
     * @param message 包含消息内容的实体对象
     * @return 包含新消息ID的Result对象
     */
    Result addMessage(Message message);
}
