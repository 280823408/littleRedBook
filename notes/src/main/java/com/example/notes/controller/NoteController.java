package com.example.notes.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;
import com.example.notes.service.INoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;
import java.util.List;

/**
 * 笔记功能控制器
 *
 * <p>功能说明：
 * 1. 处理笔记相关HTTP请求入口<br>
 * 2. 提供笔记增删改查、排序、标签查询等核心功能<br>
 * 3. 集成Redis缓存提升热点数据访问性能<br>
 * 4. 支持跨域请求访问（@CrossOrigin）<br>
 * 5. 统一返回Result标准响应格式<br>
 *
 * <p>主要接口：
 * - 按ID/用户ID/标题精确查询笔记<br>
 * - 按点赞量/创建时间全局排序<br>
 * - 按标签分类查询笔记<br>
 * - 笔记创建与更新操作<br>
 *
 * @author Mike
 * @since 2025/3/2
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("note")
public class NoteController {
    @Resource
    private INoteService noteService;

    /**
     * 根据笔记ID获取笔记详情
     * @param id 笔记唯一标识
     * @return Result对象，包含笔记详情或错误信息
     */
    @GetMapping("/{id}")
    public Result getNoteById(@PathVariable Integer id) throws ParseException {
        return noteService.getNoteById(id);
    }

    /**
     * 根据作者ID查询其所发布的笔记ID列表
     * @param authorId 作者唯一标识
     * @return Result对象，包含笔记ID列表或错误信息
     */
    @GetMapping("/authors/{authorId}/notes")
    public Result getNoteIdsByAuthorId(@PathVariable Integer authorId) {
        return noteService.getNoteIdsByAuthorId(authorId);
    }

    /**
     * 根据多个笔记ID批量获取笔记详情
     * @param ids 笔记ID集合
     * @return Result对象，包含笔记详情列表或错误信息
     */
    @GetMapping("/batch")
    public Result getNotesByIds(@RequestParam(required = false) List<Integer> ids) {
        return noteService.getNotesByIds(ids);
    }

    /**
     * 查询指定用户的所有笔记
     * @param userId 用户ID
     * @return Result对象，包含笔记列表或错误信息
     */
    @GetMapping("/users/{userId}")
    public Result getNotesByUserId(@PathVariable Integer userId) {
        return noteService.getNotesByUserId(userId);
    }

    /**
     * 根据标题关键词搜索笔记
     * @param title 笔记标题关键词
     * @return Result对象，包含匹配的笔记列表或错误信息
     */
    @GetMapping("/search")
    public Result getNotesByTitle(@RequestParam String title) {
        return noteService.getNotesByTitle(title);
    }

    /**
     * 获取全站笔记按点赞量排序
     * @return Result对象，包含排序后的笔记列表或错误信息
     */
    @GetMapping("/sorted-by-like-num")
    public Result getAllNotesSortedByLikeNum() {
        return noteService.getAllNotesSortedByLikeNum();
    }

    /**
     * 获取全站笔记按创建时间排序
     * @return Result对象，包含按时间排序的笔记列表或错误信息
     */
    @GetMapping("/sorted-by-create-time")
    public Result getAllNotesSortedByCreatTime() {
        return noteService.getAllNotesSortedByCreatTime();
    }

    /**
     * 根据标签ID查询关联笔记
     * @param tagId 标签唯一标识
     * @return Result对象，包含该标签下的笔记集合或错误信息
     */
    @GetMapping("/tags/{tagId}")
    public Result getNotesByTag(@PathVariable Integer tagId) {
        return noteService.getNotesByTag(tagId);
    }

    /**
     * 创建新笔记
     * @param note 笔记实体对象（JSON格式）
     * @return Result对象，包含新建笔记ID或错误信息
     */
    @PostMapping
    public Result addNote(@RequestBody Note note) {
        return noteService.addNote(note);
    }

    /**
     * 更新已有笔记
     * @param note 笔记实体对象（需包含ID）
     * @return Result对象，包含更新状态或错误信息
     */
    @PutMapping("/{id}")
    public Result updateNote(@PathVariable Integer id, @RequestBody Note note) {
        return noteService.updateNote(note);
    }

    /**
     * 点赞/取消点赞笔记
     * @param id 笔记ID（用于点赞记录）
     * @param userId 当前用户ID（用于点赞记录）
     * @return Result对象，包含点赞操作结果或错误信息
     */
    @PostMapping("/{id}/like")
    public Result likeNote(@PathVariable Integer id, @RequestParam Integer userId) {
        return noteService.likeNote(id, userId);
    }

    /**
     * 收藏/取消收藏笔记
     * @param id 笔记ID（用于收藏记录）
     * @param userId 当前用户ID（用于收藏记录）
     * @return Result对象，包含收藏操作结果或错误信息
     */
    @PostMapping("/{id}/collect")
    public Result collectNote(@PathVariable Integer id, @RequestParam Integer userId) {
        return noteService.collectNote(id, userId);
    }
}
