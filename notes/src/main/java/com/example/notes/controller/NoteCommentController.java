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
@RequestMapping("noteComment")
public class NoteCommentController {
    @Resource
    private INoteCommentService noteCommentService;

    /**
     * 根据ID获取评论详情
     * @param id 评论唯一标识
     * @return 包含评论实体或错误信息的Result对象
     */
    @GetMapping("getNoteCommentById")
    public Result getNoteCommentById(@RequestParam Integer id) {
        return noteCommentService.getNoteCommentById(id);
    }

    /**
     * 获取指定笔记下的所有评论
     * @param noteId 笔记唯一标识
     * @return 包含评论列表的Result对象（按时间排序）
     */
    @GetMapping("getNoteCommentByNoteId")
    public Result getNoteCommentByNoteId(@RequestParam Integer noteId) {
        return noteCommentService.getNoteCommentsByNoteId(noteId);
    }

    /**
     * 新增笔记评论
     * @param noteComment 包含评论内容、用户ID、笔记ID的传输对象
     * @return 包含新评论ID的Result对象
     */
    @PostMapping("addNoteComment")
    public Result addNoteComment(@RequestBody NoteComment noteComment) {
        return noteCommentService.addNoteComment(noteComment);
    }

    /**
     * 用户点赞评论操作
     * @param id 评论唯一标识
     * @param userId 进行点赞的用户ID
     * @return 更新后的点赞状态及点赞数Result对象
     */
    @GetMapping("likeNoteComment")
    public Result likeNoteComment(@RequestParam Integer id, @RequestParam Integer userId) {
        return noteCommentService.likeNoteComment(id, userId);
    }

    /**
     * 删除指定评论
     * @param id 评论唯一标识
     * @return 操作结果Result对象
     */
    @GetMapping("removeNoteComment")
    public Result removeNoteComment(@RequestParam Integer id) {
        return noteCommentService.removeNoteComment(id);
    }
}
