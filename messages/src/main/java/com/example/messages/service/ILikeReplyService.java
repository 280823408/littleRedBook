package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;

/**
 * 回复点赞服务接口
 *
 * <p>功能说明：
 * 1. 定义用户回复点赞行为核心业务逻辑接口<br>
 * 2. 实现回复点赞关系的持久化存储与状态维护<br>
 * 3. 支持点赞记录的增删改查及用户行为验证<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个点赞记录<br>
 * - 获取回复的全部点赞记录集合<br>
 * - 验证用户对回复的点赞状态<br>
 * - 移除指定点赞记录（取消点赞）<br>
 * - 创建用户与回复的点赞关联<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
public interface ILikeReplyService extends IService<LikeReply> {
    /**
     * 根据点赞记录ID查询详细信息
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    Result getLikeReplyById(Integer id);

    /**
     * 获取指定回复的全部点赞记录
     * @param replyId 目标回复唯一标识
     * @return 包含点赞记录集合的Result对象
     */
    Result getLikeRepliesByReplyId(Integer replyId);

    /**
     * 查询用户对回复的点赞状态
     * @param replyId 目标回复ID
     * @param userId 操作用户ID
     * @return 包含特定点赞记录的Result对象（存在即表示已点赞）
     */
    Result getLikeReplyByReplyIdAndUserId(Integer replyId, Integer userId);

    /**
     * 获取用户点赞通知
     * @param userId 操作用户ID
     * @return 包含点赞通知的Result对象
     */
    Result getLikeNotice(Integer userId);

    /**
     * 根据ID移除点赞记录
     * @param id 点赞记录唯一标识
     * @return 包含删除操作结果的Result对象
     */
    Result removeLikeReply(Integer id);

    /**
     * 新增用户回复点赞关系
     * @param likeReply 包含用户与回复关联的实体对象
     * @return 包含新增记录ID的Result对象
     */
    Result addLikeReply(LikeReply likeReply);
}
