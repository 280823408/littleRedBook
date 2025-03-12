package com.example.notes.utils;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "messages", url = "http://localhost:8102", path = "/likeComment")
public interface LikeCommentClient {
    @PostMapping("addLikeComment")
    Result addLikeComment(@RequestBody LikeComment likeComment);
    @GetMapping("getLikeCommentByCommentIdAndUserId")
    Result getLikeCommentByCommentIdAndUserId(@RequestParam Integer commentId, @RequestParam Integer userId);
    @GetMapping("removeLikeComment")
    Result removeLikeComment(@RequestParam Integer id);
}
