package com.example.littleredbook.dto;

import lombok.Data;

/**
 * 用户信息数据传输对象
 *
 * <p>功能说明：
 * 1. 用于接口层返回用户核心信息<br>
 * 2. 包含基础展示字段<br>
 * 3. 避免暴露敏感数据<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Data
public class UserDTO {
    /** 用户唯一标识 */
    private Integer id;

    /** 用户头像访问地址 */
    private String icon;

    /** 前端展示用名称 */
    private String userName;
}
