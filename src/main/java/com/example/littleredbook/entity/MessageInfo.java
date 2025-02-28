package com.example.littleredbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;
/**
 * 私信消息表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("message_info")
public class MessageInfo {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("user1_id")
  private Integer user1Id;
  @TableField("user2_id")
  private Integer user2Id;
  @TableField("inner_message")
  private String innerMessage;
  @TableField("message_time")
  private Timestamp messageTime;
}
