package com.example.notes.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.ReplyComment;
import com.example.notes.service.IReplyCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 回复评论功能控制器
 *
 * <p>功能说明：
 * 1. 处理回复评论相关HTTP请求入口<br>
 * 2. 提供回复评论查询、发布回复评论、点赞互动等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 回复评论单点查询<br>
 *   - 指定评论下的回复评论列表获取<br>
 *   - 新增回复评论记录<br>
 *   - 用户点赞回复评论操作<br>
 *   - 回复评论删除功能<br>
 *
 * @author Mike
 * @since 2025/3/14
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("reply-comments")
public class ReplyCommentController {
    @Resource
    private IReplyCommentService replyCommentService;

    /**
     * 根据ID获取回复评论详情
     * @param id 回复评论唯一标识
     * @return 包含回复评论实体或错误信息的Result对象
     */
    @GetMapping("/{id}")
    public Result getReplyCommentById(@PathVariable Integer id) {
        return replyCommentService.getReplyCommentById(id);
    }

    /**
     * 获取指定评论下的所有回复评论
     * @param commentId 评论唯一标识
     * @return 包含回复评论列表的Result对象（按时间排序）
     */
    @GetMapping("/comments/{commentId}")
    public Result getReplyCommentsByCommentId(@PathVariable Integer commentId) {
        return replyCommentService.getReplyCommentsByCommentId(commentId);
    }

    /**
     * 获取回复评论互动通知
     * @param userId 用户ID
     * @return 包含回复评论互动通知的Result对象
     */
    @GetMapping("/notices/{userId}")
    public Result getReplyCommentNotice(@PathVariable Integer userId) {
        return replyCommentService.getReplyCommentNotice(userId);
    }

    /**
     * 新增回复评论
     * @param replyComment 包含回复评论内容、用户ID、评论ID的传输对象
     * @return 包含新回复评论ID的Result对象
     */
    @PostMapping
    public Result addReplyComment(@RequestBody ReplyComment replyComment) {
        return replyCommentService.addReplyComment(replyComment);
    }

    /**
     * 用户点赞回复评论操作
     * @param id 回复评论唯一标识
     * @param userId 进行点赞的用户ID
     * @return 更新后的点赞状态及点赞数Result对象
     */
    @PostMapping("/{id}/like")
    public Result likeReplyComment(@PathVariable Integer id, @RequestParam Integer userId) {
        return replyCommentService.likeReplyComment(id, userId);
    }

    /**
     * 删除指定回复评论
     * @param id 回复评论唯一标识
     * @return 操作结果Result对象
     */
    @DeleteMapping("/{id}")
    public Result removeReplyComment(@PathVariable Integer id) {
        return replyCommentService.removeReplyComment(id);
    }
}
