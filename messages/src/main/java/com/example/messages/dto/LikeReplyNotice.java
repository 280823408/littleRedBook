package com.example.messages.dto;

import com.example.littleredbook.dto.ReplyCommentDTO;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.ReplyComment;
import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeReplyNotice {
    private ReplyCommentDTO replyComment;
    private User user;
    private Timestamp likeTime;
}
