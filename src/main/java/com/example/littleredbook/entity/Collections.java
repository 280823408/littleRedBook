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
 * 收藏记录实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("collections")
public class Collections {
  /** 收藏记录ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  /** 用户ID（关联用户表） */
  @TableField("user_id")
  private Integer userId;
  /** 笔记ID（关联笔记表） */
  @TableField("note_id")
  private Integer noteId;
  /** 收藏时间 */
  @TableField("collection_time")
  private Timestamp collectionTime;
}
