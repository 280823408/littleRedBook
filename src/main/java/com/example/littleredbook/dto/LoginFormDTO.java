package com.example.littleredbook.dto;

import lombok.Data;

@Data
public class LoginFormDTO {
    private String phone;
    private String userPassword;
    private String code;
}
