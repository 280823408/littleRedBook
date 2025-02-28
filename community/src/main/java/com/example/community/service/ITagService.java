package com.example.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Tag;
/**
 * 标签服务接口
 *
 * <p>功能说明：
 * 1. 定义标签领域核心业务逻辑接口<br>
 * 2. 实现标签数据持久化操作及业务逻辑处理<br>
 * 3. 支持单标签查询、全量标签检索、笔记关联标签查询等核心功能<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个标签信息<br>
 * - 获取所有标签列表<br>
 * - 查询指定笔记关联的所有标签<br>
 * - 插入标签与笔记的关联关系<br>
 * - 新增标签实体记录<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
public interface ITagService extends IService<Tag> {
    /**
     * 根据标签ID查询标签信息
     * @param id 标签唯一标识
     * @return 包含标签实体或错误信息的Result对象
     */
    Result getTagById(Integer id);

    /**
     * 获取系统中所有标签列表
     * @return 包含标签集合的Result对象
     */
    Result getAllTags();
    /**
     * 新增标签实体记录
     * @param tag 待新增的标签对象
     * @return 新增操作结果的Result对象
     */
    Result addTag(Tag tag);

    /**
     * 查询指定笔记关联的所有标签
     * @param noteId 笔记唯一标识
     * @return 包含关联标签列表的Result对象
     */
    Result getTagsByNoteId(Integer noteId);

    /**
     * 插入标签与笔记的关联关系
     * @param tagId 标签ID
     * @param noteId 笔记ID
     * @return 关联关系操作结果的Result对象
     */
    Result addNoteTag(Integer tagId, Integer noteId);
}
