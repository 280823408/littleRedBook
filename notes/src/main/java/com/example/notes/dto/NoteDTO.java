package com.example.notes.dto;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.example.littleredbook.entity.Tag;
import com.example.littleredbook.entity.User;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;

@Data
public class NoteDTO {
    /** 笔记ID */
    private Integer id;
    /** 笔记标题 */
    private String title;
    /** 笔记类型：0-图文，1-视频 */
    private Integer type;
    /** 公开状态：0-私密，1-公开 */
    private Integer isPublic;
    /** 资源路径（图片/视频URL） */
    private String resoure;
    /** 笔记正文内容 */
    private String content;
    /** 更新时间 */
    private Timestamp updateTime;
    /** 创建时间 */
    private Timestamp createTime;
    /** 所属用户 */
    private User user;
    /** 标签 */
    private List<Tag> tags;
    /** 点赞数 */
    private Integer likeCount;
}
