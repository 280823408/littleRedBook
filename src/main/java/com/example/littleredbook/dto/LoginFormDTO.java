package com.example.littleredbook.dto;

import lombok.Data;
/**
 * 用户登录表单数据传输对象
 *
 * <p>功能说明：
 * 1. 用于接收前端提交的登录相关数据<br>
 * 2. 支持密码登录和验证码登录两种方式<br>
 * 3. 包含必要的参数校验规则（需配合校验注解使用）<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Data
public class LoginFormDTO {
    /** 用户手机号码（登录账号） */
    private String phone;

    /** 用户登录密码 */
    private String userPassword;

    /** 短信验证码（验证码登录时必填） */
    private String code;
}
