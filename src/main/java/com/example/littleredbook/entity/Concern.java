package com.example.littleredbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 关注关系实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("concern")
public class Concern {
  /** 关注关系ID */
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  /** 粉丝用户ID（关联用户表） */
  @TableField("fans_id")
  private Integer fansId;
  /** 被关注用户ID（关联用户表） */
  @TableField("user_id")
  private Integer userId;
}
