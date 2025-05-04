package com.example.notes.utils.fallback;


import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.entity.LikeReply;
import com.example.notes.utils.MessagesClient;

/**
 * 消息服务熔断降级处理类
 *
 * <p>功能说明：
 * 1. 实现Feign客户端的服务降级逻辑<br>
 * 2. 保障消息互动操作的弹性容错能力<br>
 * 3. 提供消息服务不可用时的统一降级响应<br>
 * 4. 防止服务雪崩效应影响核心交互功能<br>
 *
 * <p>触发场景：
 * - 消息服务响应超时（默认1秒）<br>
 * - 消息服务实例不可用<br>
 * - 服务请求量超过熔断阈值<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
public class MessagesClientFallback implements MessagesClient {

    /**
     * 点赞评论添加降级处理
     * @param likeComment 点赞评论实体
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result addLikeComment(LikeComment likeComment) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 评论点赞查询降级处理
     * @param commentId 评论唯一标识
     * @param userId 用户唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getLikeCommentByCommentIdAndUserId(Integer commentId, Integer userId) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 点赞评论移除降级处理
     * @param id 点赞记录唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result removeLikeComment(Integer id) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 笔记点赞查询降级处理
     * @param noteId 笔记唯一标识
     * @param userId 用户唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getLikeNoteByNoteIdAndUserId(Integer noteId, Integer userId) {
        return Result.fail("消息服务不可用");
    }

    @Override
    public Result getLikeNoteRecordsByUserId(Integer userId) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 笔记点赞移除降级处理
     * @param id 点赞记录唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result removeLikeNote(Integer id) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 笔记点赞添加降级处理
     * @param likeNote 笔记点赞实体
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result addLikeNote(LikeNote likeNote) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 回复点赞查询降级处理
     * @param replyId 回复唯一标识
     * @param userId 用户唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getLikeReplyByReplyIdAndUserId(Integer replyId, Integer userId) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 回复点赞移除降级处理
     * @param id 点赞记录唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result removeLikeReply(Integer id) {
        return Result.fail("消息服务不可用");
    }

    /**
     * 回复点赞添加降级处理
     * @param likeReply 回复点赞实体
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result addLikeReply(LikeReply likeReply) {
        return Result.fail("消息服务不可用");
    }
}
