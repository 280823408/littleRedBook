package com.example.littleredbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
/**
 * 用户实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("user")
public class User {
    /** 用户id */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /** 用户名 */
    @TableField("user_name")
    private String userName;
    /** 用户头像图标链接 */
    private String icon;
    /** 用户个人信息描述 */
    private String info;
    /** 用户电话号码，非空 */
    private String phone;
    /** 用户密码，非空 */
    @TableField("user_password")
    private String userPassword;
    /** 粉丝数量 */
    @TableField("fans_num")
    private Double FansNum;
}
