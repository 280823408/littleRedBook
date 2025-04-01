package com.example.messages.dto;

import com.example.littleredbook.dto.NoteDTO;
import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LikeNoteNotice {
    private NoteDTO note;
    private User user;
    private Timestamp likeTime;
}
