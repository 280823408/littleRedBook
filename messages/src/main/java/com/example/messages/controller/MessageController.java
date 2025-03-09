package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Message;
import com.example.messages.service.IMessageService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.sql.Timestamp;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("messages")
public class MessageController {
    @Resource
    private IMessageService messageService;
    @GetMapping("getMessageById")
    public Result getMessageById(@RequestParam Integer id) {
        return messageService.getMessageById(id);
    }

    @GetMapping("getMessagesBySenderIdAndReceiverId")
    public Result getMessagesBySenderIdAndReceiverId(
            @RequestParam Integer senderId,
            @RequestParam Integer receiverId) {
        return messageService.getMessagesBySenderIdAndReceiverIdOrderBySendTime(senderId, receiverId);
    }

    @GetMapping("revokeMessageInLimitTime")
    public Result revokeMessageInLimitTime(@RequestBody Message message) {
        return messageService.revokeMessageInLimitTime(message);
    }

    @GetMapping("removeMessage")
    public Result removeMessage(@RequestParam Integer id) {
        return messageService.removeMessage(id);
    }

    @GetMapping("removeMessages")
    public Result removeMessages(@RequestBody List<Integer> ids) {
        return messageService.removeMessages(ids);
    }

    @GetMapping("removeMessagesInTimeInterval")
    public Result removeMessagesInTimeInterval(@RequestParam Integer senderId, @RequestParam Integer receiverId,
                                                @RequestParam Timestamp startTime, @RequestParam Timestamp endTime) {
        return messageService.removeMessagesInTimeInterval(senderId, receiverId, startTime, endTime);
    }

    @PostMapping("addMessage")
    public Result addMessage(@RequestBody Message message) {
        return messageService.addMessage(message);
    }
}
