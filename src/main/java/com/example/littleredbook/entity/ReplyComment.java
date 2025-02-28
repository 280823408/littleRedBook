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
 * 回复评论表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("reply_comment")
public class ReplyComment {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("comment_id")
  private Integer commentId;
  @TableField("user1_id")
  private  Integer user1Id;
  @TableField("user2_id")
  private  Integer user2Id;
  @TableField("like_num")
  private Integer likeNum;
  @TableField("inner_comment")
  private String innerComment;
  @TableField("comment_time")
  private Timestamp commentTime;
}
