package com.example.notes.dto;

import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ReplyNotice {
    private ReplyCommentDTO replyComment;
    private NoteCommentDTO noteComment;
    private User user;
}
