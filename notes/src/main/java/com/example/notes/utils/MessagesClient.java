package com.example.notes.utils;

import com.example.littleredbook.config.FeignConfiguration;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.entity.LikeReply;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 消息服务Feign客户端
 *
 * <p>功能说明：
 * 1. 声明式消息服务HTTP调用接口<br>
 * 2. 对接消息服务点赞模块，支持评论、笔记及回复的点赞操作<br>
 * 3. 支持服务发现与直连两种调用模式<br>
 * 4. 实现点赞记录的添加、查询、删除功能<br>
 *
 * <p>配置说明：
 * - 服务名称：messages（对应注册中心服务ID）<br>
 * - 默认直连地址：http://localhost:8102<br>
 * - 自定义配置类：FeignConfiguration（处理编解码器、拦截器等）<br>
 * - 接口模块路径划分：<br>
 *   &nbsp;&nbsp;• 评论点赞：/likeComment<br>
 *   &nbsp;&nbsp;• 笔记点赞：/likeNote<br>
 *   &nbsp;&nbsp;• 回复点赞：/likeReply<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
@FeignClient(name = "messages", url = "http://localhost:8102", configuration = FeignConfiguration.class)
public interface MessagesClient {
    /**
     * 添加评论点赞记录
     * @param likeComment 点赞评论请求体（包含用户ID、评论ID等信息）
     * @return Result标准响应（包含操作结果或错误信息）
     */
    @PostMapping("/likeComment/addLikeComment")
    Result addLikeComment(@RequestBody LikeComment likeComment);

    /**
     * 根据评论ID和用户ID查询点赞记录
     * @param commentId 评论唯一标识
     * @param userId 用户唯一标识
     * @return Result标准响应（包含LikeComment或错误信息）
     */
    @GetMapping("/likeComment/getLikeCommentByCommentIdAndUserId")
    Result getLikeCommentByCommentIdAndUserId(@RequestParam Integer commentId, @RequestParam Integer userId);

    /**
     * 移除评论点赞记录
     * @param id 点赞记录唯一标识
     * @return Result标准响应（包含操作结果或错误信息）
     */
    @GetMapping("/likeComment/removeLikeComment")
    Result removeLikeComment(@RequestParam Integer id);

    /**
     * 查询笔记点赞记录
     * @param noteId 笔记唯一标识
     * @param userId 用户唯一标识
     * @return Result标准响应（包含LikeNote或错误信息）
     */
    @GetMapping("/likeNote/getLikeNoteByNoteIdAndUserId")
    Result getLikeNoteByNoteIdAndUserId(
            @RequestParam Integer noteId,
            @RequestParam Integer userId);

    /**
     * 移除笔记点赞记录
     * @param id 点赞记录唯一标识
     * @return Result标准响应（包含操作结果或错误信息）
     */
    @GetMapping("/likeNote/removeLikeNote")
    Result removeLikeNote(@RequestParam Integer id);

    /**
     * 添加笔记点赞记录
     * @param likeNote 点赞笔记请求体（包含用户ID、笔记ID等信息）
     * @return Result标准响应（包含操作结果或错误信息）
     */
    @PostMapping("/likeNote/addLikeNote")
    Result addLikeNote(@RequestBody LikeNote likeNote);

    /**
     * 查询回复点赞记录
     * @param replyId 回复唯一标识
     * @param userId 用户唯一标识
     * @return Result标准响应（包含LikeReply或错误信息）
     */
    @GetMapping("/likeReply/getLikeReplyByReplyIdAndUserId")
    Result getLikeReplyByReplyIdAndUserId(
            @RequestParam Integer replyId,
            @RequestParam Integer userId);

    /**
     * 移除回复点赞记录
     * @param id 点赞记录唯一标识
     * @return Result标准响应（包含操作结果或错误信息）
     */
    @GetMapping("/likeReply/removeLikeReply")
    Result removeLikeReply(@RequestParam Integer id);

    /**
     * 添加回复点赞记录
     * @param likeReply 点赞回复请求体（包含用户ID、回复ID等信息）
     * @return Result标准响应（包含操作结果或错误信息）
     */
    @PostMapping("/likeReply/addLikeReply")
    Result addLikeReply(@RequestBody LikeReply likeReply);
}
