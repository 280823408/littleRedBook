package com.example.littleredbook.dto;

import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyCommentDTO {
    private Integer id;
    private Integer commentId;
    private Integer likeNum;
    private String innerComment;
    private Timestamp replyTime;
    private User user;
}
