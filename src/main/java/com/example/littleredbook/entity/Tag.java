package com.example.littleredbook.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 标签实体类
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@TableName("tag")
public class Tag {
    /** 标签id */
    @TableId(value = "id",type = IdType.AUTO)
    private Integer id;
    /** 标签内容 */
    @TableField("tag_content")
    private String tagContent;
}
