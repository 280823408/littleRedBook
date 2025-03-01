package com.example.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Tag;
import org.apache.ibatis.annotations.Select;

import java.util.List;

/**
 * 标签数据访问层接口
 *
 * <p>功能说明：
 * 1. 继承MyBatis-Plus基础Mapper实现用户数据CRUD操作<br>
 * 2. 提供默认的用户表基础数据库操作方法<br>
 * 3. 支持配合MyBatis-Plus条件构造器进行复杂查询<br>
 *
 * <p>使用规范：
 * - 需在启动类配置{@code @MapperScan}注解扫描
 * - 自定义SQL需通过XML文件或注解方式实现
 *
 * @author Mike
 * @since 2025/2/25
 */
public interface TagMapper extends BaseMapper<Tag> {
    /**
     * 根据笔记ID查询关联的标签列表
     * @param noteId
     * @return
     */
    @Select("SELECT * FROM tag JOIN tag_note ON tag.id = tag_note.tag_id WHERE tag_note.note_id = #{noteId}")
    List<Tag> selectTagsByNoteId(Integer noteId);

    /**
     * 根据标签ID查询关联的笔记ID列表
     * @param tagId
     * @return
     */
    @Select("SELECT note_id FROM tag_note WHERE tag_id = #{tagId}")
    List<Integer> selectNoteIdByTagId(Integer tagId);
}
