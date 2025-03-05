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
 * 笔记实体类
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
@TableName("note")
public class Note {
    /** 笔记ID */
    @TableId(value = "id", type = IdType.AUTO)
    private Integer id;
    /** 笔记标题 */
    private String title;
    /** 笔记类型：0-图文，1-视频 */
    private Integer type;
    /** 公开状态：0-私密，1-公开 */
    @TableField("is_public")
    private Integer isPublic;
    /** 资源路径（图片/视频URL） */
    private String resoure;
    /** 笔记正文内容 */
    private String content;
    /** 点赞数 */
    @TableField("like_num")
    private Double likeNum;
    /** 收藏数 */
    @TableField("collections_num")
    private Double collectionsNum;
    /** 更新时间 */
    @TableField("update_time")
    private Timestamp updateTime;
    /** 创建时间 */
    @TableField("create_time")
    private Timestamp createTime;
    /** 所属用户（关联字段） */
    private Integer userId;
}
