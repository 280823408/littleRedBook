package com.example.littleredbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import com.fasterxml.jackson.annotation.JsonFormat;
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
@TableName("message")
public class Message {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("sender_id")
  private Integer senderId;
  @TableField("receiver_id")
  private Integer receiverId;
  @TableField("inner_message")
  private String innerMessage;
  @TableField("send_time")
  private Timestamp sendTime;
  @TableField("is_read")
  private Boolean isRead;
}
