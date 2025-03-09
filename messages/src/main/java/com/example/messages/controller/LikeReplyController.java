package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.messages.service.ILikeReplyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("likeReply")
public class LikeReplyController {
    @Resource
    private ILikeReplyService likeReplyService;
    @GetMapping("getLikeReplyById")
    public Result getLikeReplyById(@RequestParam Integer id) {
        return likeReplyService.getLikeReplyById(id);
    }

    @GetMapping("getLikeRepliesByReplyId")
    public Result getLikeRepliesByReplyId(@RequestParam Integer replyId) {
        return likeReplyService.getLikeRepliesByReplyId(replyId);
    }

    @GetMapping("removeLikeReply")
    public Result removeLikeReply(@RequestParam Integer id) {
        return likeReplyService.removeLikeReply(id);
    }

    @PostMapping("addLikeReply")
    public Result addLikeReply(@RequestBody LikeReply likeReply) {
        return likeReplyService.addLikeReply(likeReply);
    }
}
