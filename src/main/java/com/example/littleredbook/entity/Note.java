package com.example.littleredbook.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Note {
    private Integer id;
    private String title;
    private int type;
    private int isPublic;
    private String resoure;
    private String content;
    private Timestamp updateTime;
    private Timestamp createTime;
    private User user;
}
