package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;

/**
 * 评论点赞服务接口
 *
 * <p>功能说明：
 * 1. 定义评论点赞行为核心业务逻辑接口<br>
 * 2. 实现点赞关系持久化存储与状态管理<br>
 * 3. 支持点赞记录查询、添加、删除等互动操作<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个点赞记录<br>
 * - 获取指定评论的全部点赞列表<br>
 * - 验证用户对评论的点赞状态<br>
 * - 移除点赞记录（取消点赞）<br>
 * - 新增点赞关系记录<br>
 *
 * @author Mike
 * @since 2025/3/8
 */
public interface ILikeCommentService extends IService<LikeComment> {
    /**
     * 根据点赞记录ID查询详细信息
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    Result getLikeCommentById(Integer id);

    /**
     * 获取指定评论的所有点赞记录
     * @param commentId 目标评论唯一标识
     * @return 包含点赞记录集合的Result对象
     */
    Result getLikeCommentsByCommentId(Integer commentId);

    /**
     * 查询用户对评论的点赞状态
     * @param commentId 目标评论ID
     * @param userId 操作用户ID
     * @return 包含特定点赞记录的Result对象（存在即表示已点赞）
     */
    Result getLikeCommentByCommentIdAndUserId(Integer commentId, Integer userId);

    /**
     * 根据ID移除点赞记录
     * @param id 点赞记录唯一标识
     * @return 删除操作结果的Result对象
     */
    Result removeLikeComment(Integer id);

    /**
     * 新增点赞关系记录
     * @param likeComment 包含用户与评论关联的实体对象
     * @return 包含新增记录ID的Result对象
     */
    Result addLikeComment(LikeComment likeComment);
}
