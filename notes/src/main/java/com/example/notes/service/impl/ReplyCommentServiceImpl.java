package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.ReplyComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.mapper.ReplyCommentMapper;
import com.example.notes.service.IReplyCommentService;
import com.example.notes.utils.MessagesClient;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

/**
 * 回复评论服务实现类
 *
 * <p>功能说明：
 * 1. 实现评论回复关系核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存单条回复记录<br>
 * 4. 提供回复查询、新增、删除及点赞功能<br>
 * 5. 事务注解保障数据库操作原子性<br>
 *
 * <p>关键方法：
 * - ID/评论维度回复查询<br>
 * - 带互斥锁的列表缓存查询<br>
 * - 点赞操作的缓存与数据库双写<br>
 * - 独立事务更新点赞计数器<br>
 *
 * @author Mike
 * @since 2025/3/14
 */
@Service
public class ReplyCommentServiceImpl extends ServiceImpl<ReplyCommentMapper, ReplyComment> implements IReplyCommentService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;

    /**
     * 根据ID查询回复评论详情
     * @param id 回复记录唯一标识
     * @return 包含回复实体或错误信息的Result对象
     */
    @Override
    public Result getReplyCommentById(Integer id) {
        try {
            ReplyComment replyComment = hashRedisClient.hMultiGet(CACHE_REPLYCOMMENT_KEY + id, ReplyComment.class);
            if (replyComment == null) {
                replyComment = getById(id);
                if (replyComment == null) {
                    return Result.fail("回复评论不存在");
                }
                hashRedisClient.hMultiSet(CACHE_REPLYCOMMENT_KEY + id, replyComment);
                hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(replyComment);
        } catch (ParseException e) {
            return Result.fail("获取回复评论ID为" + id + "失败");
        }
    }

    /**
     * 获取指定评论的回复列表
     * @param commentId 父评论唯一标识
     * @return 包含回复集合的Result对象
     */
    @Override
    public Result getReplyCommentsByCommentId(Integer commentId) {
        List<ReplyComment> replyComments = stringRedisClient.queryListWithMutex(
                CACHE_REPLYCOMMENT_COMMENT_KEY,
                commentId,
                ReplyComment.class,
                this::getReplyCommentsByCommentIdFromDB,
                CACHE_REPLYCOMMENT_COMMENT_TTL,
                TimeUnit.MINUTES
        );
        if (replyComments == null) {
            return Result.fail("获取回复评论列表失败");
        }
        return Result.ok(replyComments);
    }

    /**
     * 新增回复评论记录
     * @param replyComment 回复实体对象
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result addReplyComment(ReplyComment replyComment) {
        if (!this.save(replyComment)) {
            throw new RuntimeException("添加回复评论失败");
        }
        hashRedisClient.hMultiSet(CACHE_REPLYCOMMENT_KEY + replyComment.getId(), replyComment);
        hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + replyComment.getId(), CACHE_REPLYCOMMENT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 删除回复评论记录
     * @param id 回复记录唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeReplyComment(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除回复评论失败");
        }
        hashRedisClient.delete(CACHE_COMMENT_KEY + id);
        return Result.ok();
    }

    /**
     * 处理用户点赞回复操作
     * @param id 回复记录唯一标识
     * @param userId 用户唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    public Result likeReplyComment(Integer id, Integer userId) {
        Object likeReplyData =  messagesClient.getLikeReplyByReplyIdAndUserId(
                id, userId).getData();
        LikeReply likeReply = BeanUtil.mapToBean((Map<?, ?>) likeReplyData, LikeReply.class, true);
        boolean isLike = likeReply.getId() != null;
        IReplyCommentService replyCommentService = (IReplyCommentService) AopContext.currentProxy();
        replyCommentService.updateReplyCommentLikeNum(id, isLike);
        if (isLike) {
            messagesClient.removeLikeReply(likeReply.getId());
            hashRedisClient.hIncrement(CACHE_REPLYCOMMENT_KEY + id, "likeNum", -1);
            hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + id, CACHE_REPLYCOMMENT_TTL, TimeUnit.MINUTES);
            return Result.ok();
        }
        likeReply.setReplyId(id);
        likeReply.setUserId(userId);
        messagesClient.addLikeReply(likeReply);
        hashRedisClient.hIncrement(CACHE_REPLYCOMMENT_KEY + id, "likeNum", 1);
        hashRedisClient.expire(CACHE_REPLYCOMMENT_KEY + id, CACHE_REPLYCOMMENT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 更新回复点赞计数器
     * @param id 回复记录唯一标识
     * @param isLike 是否取消点赞
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result updateReplyCommentLikeNum(Integer id, boolean isLike) {
        String sql = isLike ? "like_num = like_num - 1" : "like_num = like_num + 1";
        if (!update(new LambdaUpdateWrapper<ReplyComment>()
                .eq(ReplyComment::getId, id)
                .setSql(sql))) {
            throw new RuntimeException("更新回复点赞数失败");
        }
        return Result.ok();
    }

    /**
     * 从数据库查询评论回复列表
     * @param commentId 父评论唯一标识
     * @return 按时间倒序排列的回复集合
     */
    private List<ReplyComment> getReplyCommentsByCommentIdFromDB(Integer commentId) {
        List<ReplyComment> replyCommentList = query().eq("comment_id", commentId)
                .orderByDesc("reply_time")
                .list();
        if (replyCommentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return replyCommentList;
    }
}
