package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.messages.service.ILikeCommentService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 评论点赞功能控制器
 *
 * <p>功能说明：
 * 1. 处理评论点赞相关HTTP请求入口<br>
 * 2. 提供点赞状态查询、点赞操作、取消点赞等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 根据ID查询点赞记录<br>
 *   - 根据评论ID查询所有点赞记录<br>
 *   - 根据用户与评论查询点赞状态<br>
 *   - 移除点赞记录（取消点赞）<br>
 *   - 新增点赞记录（执行点赞）<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("like-comments")
public class LikeCommentController {
    @Resource
    private ILikeCommentService likeCommentService;

    /**
     * 根据主键ID获取点赞记录详情
     *
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    @GetMapping("/{id}")
    public Result getLikeCommentById(@PathVariable Integer id) {
        return likeCommentService.getLikeCommentById(id);
    }

    /**
     * 获取指定评论的所有点赞记录
     *
     * @param commentId 被点赞的评论ID
     * @return 包含点赞记录集合的Result对象
     */
    @GetMapping("/comments/{commentId}")
    public Result getLikeCommentsByCommentId(@PathVariable Integer commentId) {
        return likeCommentService.getLikeCommentsByCommentId(commentId);
    }

    /**
     * 查询用户对指定评论的点赞状态
     *
     * @param commentId 目标评论ID
     * @param userId    操作用户ID
     * @return 包含点赞状态信息的Result对象
     */
    @GetMapping("/comments/{commentId}/users/{userId}")
    public Result getLikeCommentByCommentIdAndUserId(
            @PathVariable Integer commentId,
            @PathVariable Integer userId) {
        return likeCommentService.getLikeCommentByCommentIdAndUserId(commentId, userId);
    }

    /**
     * 获取点赞通知
     *
     * @param userId 用户ID
     * @return 包含点赞通知的Result对象
     */
    @GetMapping("/notices/{userId}")
    public Result getLikeNotice(@PathVariable Integer userId) {
        return likeCommentService.getLikeNotice(userId);
    }

    /**
     * 移除点赞记录（取消点赞操作）
     *
     * @param id 点赞记录唯一标识
     * @return 操作结果的Result对象
     */
    @DeleteMapping("/{id}")
    public Result removeLikeComment(@PathVariable Integer id) {
        return likeCommentService.removeLikeComment(id);
    }

    /**
     * 新增评论点赞记录（执行点赞操作）
     *
     * @param likeComment 点赞记录数据传输对象（需包含commentId和userId）
     * @return 操作结果的Result对象
     */
    @PostMapping
    public Result addLikeComment(@RequestBody LikeComment likeComment) {
        return likeCommentService.addLikeComment(likeComment);
    }
}
