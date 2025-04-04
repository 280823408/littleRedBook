package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Message;
import com.example.messages.service.IMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

/**
 * 站内消息功能控制器
 *
 * <p>功能说明：
 * 1. 处理用户消息相关HTTP请求入口<br>
 * 2. 提供消息收发、消息管理、消息撤回等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 消息单点查询<br>
 *   - 会话消息记录获取<br>
 *   - 限时消息撤回<br>
 *   - 单条/批量消息删除<br>
 *   - 时间区间消息清除<br>
 *   - 新消息发送<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("message")
public class MessageController {
    @Resource
    private IMessageService messageService;

    /**
     * 根据消息ID获取消息详情
     *
     * @param id 消息唯一标识
     * @return 包含消息实体或错误信息的Result对象
     */
    @GetMapping("/{id}")
    public Result getMessageById(@PathVariable Integer id) {
        return messageService.getMessageById(id);
    }

    /**
     * 获取未读消息列表
     *
     * @param receiverId 接收方用户ID
     * @return 未读消息列表Result对象
     */
    @GetMapping("/receiver/unread/{receiverId}")
    public Result getMessagesByReceiverIdWithNoRead(@PathVariable Integer receiverId) {
        return messageService.getMessagesByReceiverIdWithNoRead(receiverId);
    }

    /**
     * 获取指定用户间的完整会话记录
     *
     * @param senderId   发送方用户ID
     * @param receiverId 接收方用户ID
     * @return 按发送时间排序的消息列表Result对象
     */
    @GetMapping("/conversations/{senderId}/{receiverId}")
    public Result getMessagesBySenderIdAndReceiverId(
            @PathVariable Integer senderId,
            @PathVariable Integer receiverId) {
        return messageService.getMessagesBySenderIdAndReceiverIdOrderBySendTime(senderId, receiverId);
    }

    /**
     * 获取用户消息通知
     *
     * @param userId 用户ID
     * @return 消息通知Result对象
     */
    @GetMapping("/notices/{userId}")
    public Result getMessagesNotices(@PathVariable Integer userId) {
        return messageService.getMessagesNotices(userId);
    }

    /**
     * 在允许的时间范围内撤回消息
     *
     * @param message 包含消息ID和必要验证字段的消息对象
     * @return 撤回操作结果Result对象
     */
    @PostMapping("/revoke")
    public Result revokeMessageInLimitTime(@RequestBody Message message) {
        return messageService.revokeMessageInLimitTime(message);
    }

    /**
     * 删除单条消息记录
     *
     * @param id 消息唯一标识
     * @return 删除操作结果Result对象
     */
    @DeleteMapping("/{id}")
    public Result removeMessage(@PathVariable Integer id) {
        return messageService.removeMessage(id);
    }

    /**
     * 批量删除消息记录
     *
     * @param ids 需要删除的消息ID列表
     * @return 批量删除操作结果Result对象
     */
    @DeleteMapping
    public Result removeMessages(@RequestBody List<Integer> ids) {
        return messageService.removeMessages(ids);
    }

    /**
     * 删除指定时间区间内的会话消息
     *
     * @param senderId   发送方用户ID
     * @param receiverId 接收方用户ID
     * @param startTime  时间区间开始时间戳
     * @param endTime    时间区间结束时间戳
     * @return 删除操作结果Result对象
     */
    @DeleteMapping("/conversations/{senderId}/{receiverId}")
    public Result removeMessagesInTimeInterval(
            @PathVariable Integer senderId,
            @PathVariable Integer receiverId,
            @RequestParam Timestamp startTime,
            @RequestParam Timestamp endTime) {
        return messageService.removeMessagesInTimeInterval(senderId, receiverId, startTime, endTime);
    }

    /**
     * 发送新消息
     *
     * @param message 包含发送方、接收方、内容的消息对象
     * @return 消息发送结果Result对象（包含新消息ID）
     */
    @PostMapping
    public Result addMessage(@RequestBody Message message) {
        return messageService.addMessage(message);
    }
}
