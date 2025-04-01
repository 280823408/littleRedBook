package com.example.notes.dto;
import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CommentNotice {
    private NoteCommentDTO noteComment;
    private NoteDTO note;
    private User user;
}
