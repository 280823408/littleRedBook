package com.example.notes.dto;

import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class NoteCommentDTO {
    private Integer id;
    private Integer noteId;
    private Integer likeNum;
    private String innerComment;
    private Timestamp commentTime;
    private User user;
}
