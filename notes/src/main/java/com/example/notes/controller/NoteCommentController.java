package com.example.notes.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.NoteComment;
import com.example.notes.service.INoteCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记评论功能控制器
 *
 * <p>功能说明：
 * 1. 处理笔记评论相关HTTP请求入口<br>
 * 2. 提供评论查询、发布评论、点赞互动等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 评论单点查询<br>
 *   - 笔记评论列表获取<br>
 *   - 新增评论记录<br>
 *   - 用户点赞评论操作<br>
 *   - 评论删除功能<br>
 *
 * @author Mike
 * @since 2025/3/12
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("note-comments")
public class NoteCommentController {
    @Resource
    private INoteCommentService noteCommentService;

    /**
     * 根据ID获取评论详情
     * @param id 评论唯一标识
     * @return 包含评论实体或错误信息的Result对象
     */
    @GetMapping("/{id}")
    public Result getNoteCommentById(@PathVariable Integer id) {
        return noteCommentService.getNoteCommentById(id);
    }

    /**
     * 获取指定笔记下的所有评论
     * @param noteId 笔记唯一标识
     * @return 包含评论列表的Result对象（按时间排序）
     */
    @GetMapping("/notes/{noteId}")
    public Result getNoteCommentByNoteId(@PathVariable Integer noteId) {
        return noteCommentService.getNoteCommentsByNoteId(noteId);
    }

    /**
     * 获取指定用户下的所有评论
     * @param userId 用户唯一标识
     * @return 包含评论列表的Result对象（按时间排序）
     */
    @GetMapping("/users/{userId}")
    public Result getNoteCommentByUserId(@PathVariable Integer userId) {
        return noteCommentService.getNoteCommentsByUserId(userId);
    }

    /**
     * 获取用户评论互动通知
     * @param userId 用户唯一标识
     * @return 包含评论互动通知的Result对象
     */
    @GetMapping("/notices/{userId}")
    public Result getNoteCommentNotice(@PathVariable Integer userId) {
        return noteCommentService.getNoteCommentNotice(userId);
    }

    /**
     * 新增笔记评论
     * @param noteComment 包含评论内容、用户ID、笔记ID的传输对象
     * @return 包含新评论ID的Result对象
     */
    @PostMapping
    public Result addNoteComment(@RequestBody NoteComment noteComment) {
        return noteCommentService.addNoteComment(noteComment);
    }

    /**
     * 用户点赞评论操作
     * @param id 评论唯一标识
     * @param userId 进行点赞的用户ID
     * @return 更新后的点赞状态及点赞数Result对象
     */
    @PostMapping("/{id}/like")
    public Result likeNoteComment(@PathVariable Integer id, @RequestParam Integer userId) {
        return noteCommentService.likeNoteComment(id, userId);
    }

    /**
     * 删除指定评论
     * @param id 评论唯一标识
     * @return 操作结果Result对象
     */
    @DeleteMapping("/{id}")
    public Result removeNoteComment(@PathVariable Integer id) {
        return noteCommentService.removeNoteComment(id);
    }
}
