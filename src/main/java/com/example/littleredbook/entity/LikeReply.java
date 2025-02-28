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
 * 点赞回复表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("like_reply")
public class LikeReply {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("reply_id")
  private Integer replyId;
  @TableField("user1_id")
  private Integer user1Id;
  @TableField("user2_id")
  private Integer user2Id;
  @TableField("like_time")
  private Timestamp likeTime;
}
