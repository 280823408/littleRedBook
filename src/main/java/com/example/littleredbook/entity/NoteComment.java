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
 * 趣记评论表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("note_comment")
public class NoteComment {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("note_id")
  private Integer noteId;
  @TableField("user1_id")
  private Integer user1Id;
  @TableField("user2_id")
  private Integer user2Id;
  @TableField("like_num")
  private Integer likeNum;
  @TableField("inner_comment")
  private String innerComment;
  @TableField("comment_time")
  private Timestamp commentTime;
}
