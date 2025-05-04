package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;
import com.example.messages.service.ILikeNoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 笔记点赞功能控制器
 *
 * <p>功能说明：
 * 1. 处理笔记点赞相关HTTP请求入口<br>
 * 2. 提供点赞状态查询、点赞操作、取消点赞等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 根据ID查询点赞记录<br>
 *   - 根据笔记ID查询所有点赞记录<br>
 *   - 根据用户与笔记查询点赞状态<br>
 *   - 移除点赞记录（取消点赞）<br>
 *   - 新增点赞记录（执行点赞）<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("like-notes")
public class LikeNoteController {
    @Resource
    private ILikeNoteService likeNoteService;

    /**
     * 根据主键ID获取点赞记录详情
     *
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    @GetMapping("/{id}")
    public Result getLikeNoteById(@PathVariable Integer id) {
        return likeNoteService.getLikeNoteById(id);
    }

    /**
     * 获取指定笔记的所有点赞记录
     *
     * @param noteId 被点赞的笔记ID
     * @return 包含点赞记录集合的Result对象
     */
    @GetMapping("/notes/{noteId}")
    public Result getLikeNotesByNoteId(@PathVariable Integer noteId) {
        return likeNoteService.getLikeNotesByNoteId(noteId);
    }

    /**
     * 获取指定用户的所有点赞记录
     *
     * @param userId 操作用户ID
     * @return 包含点赞记录集合的Result对象
     */
    @GetMapping("/users/{userId}")
    public Result getLikesNotesByUserId(@PathVariable Integer userId) {
        return likeNoteService.getLikesNotesByUserId(userId);
    }

    /**
     * 获取点赞记录的记录
     *
     * @param userId 操作用户ID
     * @return 包含点赞记录集合的Result对象
     */
    @GetMapping("/records/{userId}")
    public Result getLikeNoteRecordsByUserId(@PathVariable Integer userId) {
        return likeNoteService.getLikeNoteRecordsByUserId(userId);
    }

    /**
     * 查询用户对指定笔记的点赞状态
     *
     * @param noteId 目标笔记ID
     * @param userId 操作用户ID
     * @return 包含点赞状态信息的Result对象
     */
    @GetMapping("/notes/{noteId}/users/{userId}")
    public Result getLikeNoteByNoteIdAndUserId(
            @PathVariable Integer noteId,
            @PathVariable Integer userId) {
        return likeNoteService.getLikeNoteByNoteIdAndUserId(noteId, userId);
    }

    @GetMapping("/notices/{userId}")
    public Result getLikeNotice(@PathVariable Integer userId) {
        return likeNoteService.getLikeNotice(userId);
    }

    /**
     * 移除点赞记录（取消点赞操作）
     *
     * @param id 点赞记录唯一标识
     * @return 操作结果的Result对象
     */
    @DeleteMapping("/{id}")
    public Result removeLikeNote(@PathVariable Integer id) {
        return likeNoteService.removeLikeNote(id);
    }

    /**
     * 新增笔记点赞记录（执行点赞操作）
     *
     * @param likeNote 点赞记录数据传输对象（需包含noteId和userId）
     * @return 操作结果的Result对象
     */
    @PostMapping
    public Result addLikeNote(@RequestBody LikeNote likeNote) {
        return likeNoteService.addLikeNote(likeNote);
    }
}
