package com.example.notes.utils;

import com.example.littleredbook.config.FeignConfiguration;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.entity.LikeReply;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "messages", url = "http://localhost:8102", configuration = FeignConfiguration.class)
public interface MessagesClient {
    @PostMapping("/likeComment/addLikeComment")
    Result addLikeComment(@RequestBody LikeComment likeComment);

    @GetMapping("/likeComment/getLikeCommentByCommentIdAndUserId")
    Result getLikeCommentByCommentIdAndUserId(@RequestParam Integer commentId, @RequestParam Integer userId);

    @GetMapping("/likeComment/removeLikeComment")
    Result removeLikeComment(@RequestParam Integer id);

    @GetMapping("/likeNote/getLikeNoteByNoteIdAndUserId")
    Result getLikeNoteByNoteIdAndUserId(
            @RequestParam Integer noteId,
            @RequestParam Integer userId);
    @GetMapping("/likeNote/removeLikeNote")
    Result removeLikeNote(@RequestParam Integer id);

    @PostMapping("/likeNote/addLikeNote")
    Result addLikeNote(@RequestBody LikeNote likeNote);

    @GetMapping("/likeReply/getLikeReplyByReplyIdAndUserId")
    Result getLikeReplyByReplyIdAndUserId(
            @RequestParam Integer replyId,
            @RequestParam Integer userId);
    @GetMapping("/likeReply/removeLikeReply")
    Result removeLikeReply(@RequestParam Integer id);

    @PostMapping("/likeReply/addLikeReply")
    Result addLikeReply(@RequestBody LikeReply likeReply);
}
