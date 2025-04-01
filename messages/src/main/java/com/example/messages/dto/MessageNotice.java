package com.example.messages.dto;

import com.example.littleredbook.entity.Message;
import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class MessageNotice {
    private User user;
    private Message message;
    private Timestamp sendTime;
}
