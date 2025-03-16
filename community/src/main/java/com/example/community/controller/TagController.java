package com.example.community.controller;

import com.example.community.service.ITagService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Tag;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 标签功能控制器
 *
 * <p>功能说明：
 * 1. 处理标签模块相关HTTP请求入口<br>
 * 2. 提供标签查询、新增、关联笔记等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 标签单点查询<br>
 *   - 全量标签列表获取<br>
 *   - 新增标签操作<br>
 *   - 笔记关联标签查询<br>
 *   - 标签与笔记关联关系创建<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("tag")
public class TagController {
    @Resource
    private ITagService tagService;

    /**
     * 获取指定ID的标签信息
     *
     * @param id 标签唯一标识
     * @return 包含标签实体或错误信息的Result对象
     */
    @GetMapping("getTagById")
    public Result getTagById(@RequestParam Integer id) {
        return tagService.getTagById(id);
    }

    /**
     * 获取系统中所有标签列表
     *
     * @return 包含标签集合的Result对象
     */
    @GetMapping("getAllTags")
    public Result getAllTags() {
        return tagService.getAllTags();
    }

    /**
     * 新增标签实体记录
     *
     * @param tag 待新增的标签数据传输对象
     * @return 操作结果的Result对象
     */
    @PostMapping("addTag")
    public Result addTag(@RequestBody Tag tag) {
        return tagService.addTag(tag);
    }

    /**
     * 获取指定笔记关联的所有标签
     *
     * @param noteId 笔记唯一标识
     * @return 包含关联标签列表的Result对象
     */
    @GetMapping ("getTagsByNoteId")
    public Result getTagsByNoteId(@RequestParam Integer noteId) {
        return tagService.getTagsByNoteId(noteId);
    }

    /**
     * 获取指定标签关联的所有笔记ID
     *
     * @param tagId 标签唯一标识
     * @return 包含关联标签列表的Result对象
     */
    @GetMapping("getNoteIdByTagId")
    public Result getNoteIdByTagId(@RequestParam Integer tagId) {
        return tagService.getNoteIdByTagId(tagId);
    }

    /**
     * 创建标签与笔记的关联关系
     *
     * @param tagId 标签ID
     * @param noteId 笔记ID
     * @return 关联操作结果的Result对象
     */
    @GetMapping("addNoteTag")
    public Result addNoteTag(@RequestParam Integer tagId, @RequestParam Integer noteId) {
        return tagService.addNoteTag(tagId, noteId);
    }
}
