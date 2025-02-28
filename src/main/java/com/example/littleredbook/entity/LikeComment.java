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
 * 点赞评论实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("like_comment")
public class LikeComment {
  /** 点赞记录ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  /** 评论ID（关联评论表） */
  @TableField("comment_id")
  private Integer commentId;
  /** 点赞用户ID（关联用户表） */
  @TableField("user1_id")
  private Integer user1Id;
  /** 被点赞用户ID（关联用户表） */
  @TableField("user2_id")
  private Integer user2Id;
  /** 点赞时间 */
  @TableField("like_time")
  private Timestamp likeTime;
}
