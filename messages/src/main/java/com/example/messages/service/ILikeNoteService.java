package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;

/**
 * 笔记点赞服务接口
 *
 * <p>功能说明：
 * 1. 定义用户笔记点赞行为核心业务逻辑接口<br>
 * 2. 实现点赞关系数据持久化存储与状态维护<br>
 * 3. 支持点赞记录查询、添加、移除等互动行为管理<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个点赞记录<br>
 * - 获取笔记的全部点赞记录集合<br>
 * - 验证用户对笔记的点赞状态<br>
 * - 移除指定点赞记录（取消点赞）<br>
 * - 创建用户与笔记的点赞关联<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
public interface ILikeNoteService extends IService<LikeNote> {
    /**
     * 根据点赞记录ID查询详细信息
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    Result getLikeNoteById(Integer id);

    /**
     * 获取指定笔记的全部点赞记录
     * @param noteId 目标笔记唯一标识
     * @return 包含点赞记录集合的Result对象
     */
    Result getLikeNotesByNoteId(Integer noteId);

    /**
     * 查询用户对笔记的点赞状态
     * @param noteId 目标笔记ID
     * @param userId 操作用户ID
     * @return 包含特定点赞记录的Result对象（存在即表示已点赞）
     */
    Result getLikeNoteByNoteIdAndUserId(Integer noteId, Integer userId);

    /**
     * 根据ID移除点赞记录
     * @param id 点赞记录唯一标识
     * @return 包含删除操作结果的Result对象
     */
    Result removeLikeNote(Integer id);

    /**
     * 新增用户笔记点赞关系
     * @param likeNote 包含用户与笔记关联的实体对象
     * @return 包含新增记录ID的Result对象
     */
    Result addLikeNote(LikeNote likeNote);
}
