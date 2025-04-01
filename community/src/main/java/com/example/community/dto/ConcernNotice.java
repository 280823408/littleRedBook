package com.example.community.dto;

import com.example.littleredbook.entity.User;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ConcernNotice {
    private User user;
    private Timestamp concernTime;
}
