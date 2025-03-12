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
@RequestMapping("likeNote")
public class LikeNoteController {
    @Resource
    private ILikeNoteService likeNoteService;

    /**
     * 根据主键ID获取点赞记录详情
     *
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    @GetMapping("getLikeNoteById")
    public Result getLikeNoteById(@RequestParam Integer id) {
        return likeNoteService.getLikeNoteById(id);
    }

    /**
     * 获取指定笔记的所有点赞记录
     *
     * @param noteId 被点赞的笔记ID
     * @return 包含点赞记录集合的Result对象
     */
    @GetMapping("getLikeNotesByNoteId")
    public Result getLikeNotesByNoteId(@RequestParam Integer noteId) {
        return likeNoteService.getLikeNotesByNoteId(noteId);
    }

    /**
     * 查询用户对指定笔记的点赞状态
     * （TODO 待测试接口）
     *
     * @param noteId 目标笔记ID
     * @param userId 操作用户ID
     * @return 包含点赞状态信息的Result对象
     */
    @GetMapping("getLikeNoteByNoteIdAndUserId")
    public Result getLikeNoteByNoteIdAndUserId(
            @RequestParam Integer noteId,
            @RequestParam Integer userId) {
        return likeNoteService.getLikeNoteByNoteIdAndUserId(noteId, userId);
    }

    /**
     * 移除点赞记录（取消点赞操作）
     *
     * @param id 点赞记录唯一标识
     * @return 操作结果的Result对象
     */
    @GetMapping("removeLikeNote")
    public Result removeLikeNote(@RequestParam Integer id) {
        return likeNoteService.removeLikeNote(id);
    }

    /**
     * 新增笔记点赞记录（执行点赞操作）
     *
     * @param likeNote 点赞记录数据传输对象（需包含noteId和userId）
     * @return 操作结果的Result对象
     */
    @PostMapping("addLikeNote")
    public Result addLikeNote(@RequestBody LikeNote likeNote) {
        return likeNoteService.addLikeNote(likeNote);
    }
}

