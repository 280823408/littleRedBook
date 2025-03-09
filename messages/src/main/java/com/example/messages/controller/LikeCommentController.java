package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.messages.service.ILikeCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("likeComment")
public class LikeCommentController {
    @Resource
    private ILikeCommentService likeCommentService;
    @GetMapping("getLikeCommentById")
    public Result getLikeCommentById(@RequestParam Integer id) {
        return likeCommentService.getLikeCommentById(id);
    }

    @GetMapping("getLikeCommentsByCommentId")
    public Result getLikeCommentsByCommentId(@RequestParam Integer commentId) {
        return likeCommentService.getLikeCommentsByCommentId(commentId);
    }

    @GetMapping("removeLikeComment")
    public Result removeLikeComment(@RequestParam Integer id) {
        return likeCommentService.removeLikeComment(id);
    }

    @PostMapping("addLikeComment")
    public Result addLikeComment(@RequestBody LikeComment likeComment) {
        return likeCommentService.addLikeComment(likeComment);
    }
}
