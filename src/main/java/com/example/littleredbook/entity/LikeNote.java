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
 * 点赞趣记表
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("like_note")
public class LikeNote {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("note_id")
  private Integer noteId;
  @TableField("user_id")
  private Integer userId;
  @TableField("like_time")
  private Timestamp likeTime;
}
