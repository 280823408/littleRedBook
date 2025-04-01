package com.example.messages.dto;

import com.example.littleredbook.dto.NoteCommentDTO;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeCommentNotice {
    private NoteCommentDTO noteComment;
    private User user;
    private Timestamp likeTime;
}
