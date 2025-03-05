package com.example.notes.dto;

import com.example.littleredbook.entity.Tag;
import com.example.littleredbook.entity.User;
import lombok.Data;

import java.sql.Timestamp;
import java.util.List;
/**
 * 笔记信息数据传输对象
 *
 * <p>功能说明：
 * 1. 用于接口层返回笔记核心信息及关联数据<br>
 * 2. 包含完整的笔记展示字段和嵌套对象<br>
 * 3. 支持多种内容类型（图文/视频）的标准化传输<br>
 * 4. 避免暴露内部系统字段（如数据库ID等敏感信息）<br>
 *
 * <p>设计特性：
 * - 时间字段精确到毫秒级（Timestamp类型）<br>
 * - 点赞数采用Double类型支持热度算法扩展<br>
 * - 嵌套完整用户对象和标签集合<br>
 *
 * @author Mike
 * @since 2025/2/28
 */
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

    /** 点赞数 */
    private Double likeNum;

    /** 收藏数 */
    private Double collectionsNum;

    /** 所属用户 */
    private User user;

    /** 标签 */
    private List<Tag> tags;
}
