package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.messages.service.ILikeReplyService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 回复点赞功能控制器
 *
 * <p>功能说明：
 * 1. 处理回复点赞相关HTTP请求入口<br>
 * 2. 提供点赞状态查询、点赞操作、取消点赞等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 根据ID查询点赞记录<br>
 *   - 根据回复ID查询所有点赞记录<br>
 *   - 根据用户与回复查询点赞状态<br>
 *   - 移除点赞记录（取消点赞）<br>
 *   - 新增点赞记录（执行点赞）<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("likeReply")
public class LikeReplyController {
    @Resource
    private ILikeReplyService likeReplyService;

    /**
     * 根据主键ID获取点赞记录详情
     *
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    @GetMapping("getLikeReplyById")
    public Result getLikeReplyById(@RequestParam Integer id) {
        return likeReplyService.getLikeReplyById(id);
    }

    /**
     * 获取指定回复的所有点赞记录
     *
     * @param replyId 被点赞的回复ID
     * @return 包含点赞记录集合的Result对象
     */
    @GetMapping("getLikeRepliesByReplyId")
    public Result getLikeRepliesByReplyId(@RequestParam Integer replyId) {
        return likeReplyService.getLikeRepliesByReplyId(replyId);
    }

    /**
     * 查询用户对指定回复的点赞状态
     * （TODO 待测试接口）
     *
     * @param replyId 目标回复ID
     * @param userId  操作用户ID
     * @return 包含点赞状态信息的Result对象
     */
    @GetMapping("getLikeReplyByReplyIdAndUserId")
    public Result getLikeReplyByReplyIdAndUserId(
            @RequestParam Integer replyId,
            @RequestParam Integer userId) {
        return likeReplyService.getLikeReplyByReplyIdAndUserId(replyId, userId);
    }

    /**
     * 移除点赞记录（取消点赞操作）
     *
     * @param id 点赞记录唯一标识
     * @return 操作结果的Result对象
     */
    @GetMapping("removeLikeReply")
    public Result removeLikeReply(@RequestParam Integer id) {
        return likeReplyService.removeLikeReply(id);
    }

    /**
     * 新增回复点赞记录（执行点赞操作）
     *
     * @param likeReply 点赞记录数据传输对象（需包含replyId和userId）
     * @return 操作结果的Result对象
     */
    @PostMapping("addLikeReply")
    public Result addLikeReply(@RequestBody LikeReply likeReply) {
        return likeReplyService.addLikeReply(likeReply);
    }
}
