package com.example.littleredbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.sql.Timestamp;

@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("search_record")
public class SearchRecord {
  @TableId(value = "id", type = IdType.AUTO)
  private Integer id;
  @TableField("user_id")
  private Integer userId;
  private String keyword;
  @TableField("search_time")
  private Timestamp searchTime;
}
