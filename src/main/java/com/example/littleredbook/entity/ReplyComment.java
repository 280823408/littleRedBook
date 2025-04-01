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
  @TableField("like_num")
  private Integer likeNum;
  @TableField("inner_comment")
  private String innerComment;
  @TableField("reply_time")
  private Timestamp replyTime;
  @TableField("user_id")
  private  Integer userId;
}
