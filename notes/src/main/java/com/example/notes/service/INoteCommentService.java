package com.example.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.NoteComment;

/**
 * 笔记评论服务接口
 *
 * <p>功能说明：
 * 1. 定义笔记评论领域核心业务逻辑接口<br>
 * 2. 实现评论数据持久化操作及互动行为管理<br>
 * 3. 支持评论查询、新增、删除、点赞等核心互动功能<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个评论详情<br>
 * - 获取指定笔记下的全部评论<br>
 * - 新增笔记评论记录<br>
 * - 根据ID删除评论记录<br>
 * - 用户点赞/取消点赞评论操作<br>
 * - 更新评论点赞数量<br>
 *
 * @author Mike
 * @since 2025/3/14
 */
public interface INoteCommentService extends IService<NoteComment> {
    /**
     * 根据评论ID查询详细信息
     * @param id 评论唯一标识
     * @return 包含评论实体或错误信息的Result对象
     */
    Result getNoteCommentById(Integer id);

    /**
     * 获取指定笔记的全部评论（按时间倒序）
     * @param noteId 关联笔记唯一标识
     * @return 包含评论列表的Result对象
     */
    Result getNoteCommentsByNoteId(Integer noteId);

    /**
     * 获取指定用户的全部评论（按时间倒序）
     * @param userId 用户ID
     * @return 包含评论列表的Result对象
     */
    Result getNoteCommentsByUserId(Integer userId);

    /**
     * 获取用户点赞的评论通知
     * @param userId 用户ID
     * @return 包含点赞评论列表的Result对象
     */
    Result getNoteCommentNotice(Integer userId);

    /**
     * 新增笔记评论记录
     * @param noteComment 包含评论内容的实体对象
     * @return 包含新增评论ID的Result对象
     */
    Result addNoteComment(NoteComment noteComment);

    /**
     * 根据评论ID删除记录
     * @param id 评论唯一标识
     * @return 删除操作结果的Result对象
     */
    Result removeNoteComment(Integer id);

    /**
     * 用户点赞/取消点赞评论操作
     * @param id 评论唯一标识
     * @param userId 操作用户ID
     * @return 包含最新点赞状态的Result对象
     */
    Result likeNoteComment(Integer id, Integer userId);

    /**
     * 更新评论点赞计数（内部调用）
     * @param id 评论唯一标识
     * @param isLike 是否增加点赞（true:点赞 false:取消）
     * @return 包含最新点赞数量的Result对象
     */
    Result updateNoteCommentLikeNum(Integer id, boolean isLike);
}
