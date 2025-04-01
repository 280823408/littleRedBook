package com.example.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.ReplyComment;

/**
 * 回复评论服务接口
 *
 * <p>功能说明：
 * 1. 定义评论回复领域核心业务逻辑接口<br>
 * 2. 实现回复评论的增删改查及点赞互动管理<br>
 * 3. 支持多级评论结构下的回复操作与统计<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个回复评论<br>
 * - 获取指定主评论下的所有回复评论<br>
 * - 新增回复评论记录<br>
 * - 根据ID删除回复评论<br>
 * - 用户点赞/取消点赞回复评论操作<br>
 * - 更新回复评论点赞数量<br>
 *
 * @author Mike
 * @since 2025/3/14
 */
public interface IReplyCommentService extends IService<ReplyComment> {
    /**
     * 根据ID查询回复评论详情
     * @param id 回复评论唯一标识
     * @return 包含回复评论实体或错误信息的Result对象
     */
    Result getReplyCommentById(Integer id);

    /**
     * 获取指定主评论下的所有回复（按时间正序排列）
     * @param commentId 上级评论ID
     * @return 包含回复评论列表的Result对象
     */
    Result getReplyCommentsByCommentId(Integer commentId);

    /**
     * 获取回复评论通知
     * @param userId 操作用户ID
     * @return 包含回复评论通知的Result对象
     */
    Result getReplyCommentNotice(Integer userId);

    /**
     * 新增回复评论记录
     * @param replyComment 包含回复内容的实体对象
     * @return 包含新增回复ID的Result对象
     */
    Result addReplyComment(ReplyComment replyComment);

    /**
     * 根据ID删除回复评论
     * @param id 回复评论唯一标识
     * @return 删除操作结果的Result对象
     */
    Result removeReplyComment(Integer id);

    /**
     * 用户点赞/取消点赞回复评论
     * @param id 回复评论唯一标识
     * @param userId 操作用户ID
     * @return 包含最新点赞状态的Result对象
     */
    Result likeReplyComment(Integer id, Integer userId);

    /**
     * 更新回复评论点赞数（内部服务调用）
     * @param id 回复评论唯一标识
     * @param isLike 是否增加点赞（true:点赞 false:取消）
     * @return 包含最新点赞数的Result对象
     */
    Result updateReplyCommentLikeNum(Integer id, boolean isLike);
}
