package com.example.messages.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.NoteCommentDTO;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.messages.dto.LikeCommentNotice;
import com.example.messages.mapper.LikeCommentMapper;
import com.example.messages.service.ILikeCommentService;
import com.example.messages.utils.NotesClient;
import com.example.messages.utils.UserCenterClient;
import jakarta.annotation.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.*;

/**
 * 评论点赞服务实现类
 *
 * <p>功能说明：
 * 1. 实现评论点赞关系核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存点赞记录<br>
 * 4. 设计复合键二级索引优化联合查询<br>
 * 5. 事务注解保障数据操作原子性<br>
 * 6. 消息队列异步处理点赞记录缓存更新<br>
 *
 * <p>关键特性：
 * - 主记录缓存与二级索引同步维护<br>
 * - 带互斥锁的列表缓存查询机制<br>
 * - 双删策略保障缓存一致性<br>
 * - 时间维度数据自动生成<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Service
public class LikeCommentServiceImpl extends ServiceImpl<LikeCommentMapper, LikeComment> implements ILikeCommentService {
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MQClient mqClient;
    @Resource
    private UserCenterClient userCenterClient;
    @Resource
    private NotesClient notesClient;

    /**
     * 根据主键查询点赞记录
     * @param id 点赞记录唯一标识
     * @return 包含点赞实体或错误信息的Result对象
     */
    @Override
    public Result getLikeCommentById(Integer id) {
        try {
            LikeComment likeComment = hashRedisClient.hMultiGet(CACHE_LIKECOMMENT_KEY + id, LikeComment.class);
            if (likeComment != null) {
                return Result.ok(likeComment);
            }
            likeComment = getById(id);
            if (likeComment == null) {
                return Result.fail("该点赞评论记录ID不存在");
            }
            hashRedisClient.hSet(CACHE_LIKECOMMENT_COMMENT_USER_KEY + likeComment.getCommentId() + ":" + likeComment.getUserId()
                    ,"id", likeComment.getId(), CACHE_LIKECOMMENT_COMMENT_USER_TTL, TimeUnit.MINUTES);
            mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_ADD_QUEUE_ROUTING_KEY, likeComment);
            return Result.ok(likeComment);
        } catch (ParseException e) {
            return Result.fail("获取点赞评论记录ID为" + id + "失败");
        }
    }

    /**
     * 获取评论所有点赞记录
     * @param commentId 被点赞评论ID
     * @return 包含点赞集合的Result对象
     */
    @Override
    public Result getLikeCommentsByCommentId(Integer commentId) {
        List<Integer> likeCommentListIds = listObjs(query().getWrapper()
                .eq("comment_id", commentId)
                .select("id"));
        List<LikeComment> likeCommentList = new ArrayList<>();
        for(Integer id : likeCommentListIds) {
            Result result = this.getLikeCommentById(id);
            if (result.getSuccess()) {
                likeCommentList.add((LikeComment) result.getData());
            }
        }
        return Result.ok(likeCommentList);
    }

    /**
     * 联合查询用户对评论的点赞状态
     * @param commentId 目标评论ID
     * @param userId 查询用户ID
     * @return 包含点赞记录的Result对象
     */
    // TODO 可以在Redis中设计LikeComment的二级索引，即通过commentId+userId -> id的映射关系，从而达到使用Redis优化查询的目的
    // TODO 本方法未处理缓存穿透和缓存击穿（待后续优化）
    @Override
    public Result getLikeCommentByCommentIdAndUserId(Integer commentId, Integer userId) {
        String key = CACHE_LIKECOMMENT_COMMENT_USER_KEY + commentId + ":" + userId;
        try{
            Integer id = hashRedisClient.hMultiGet(key, Integer.class);
            LikeComment likeComment = null;
            if (id != null) {
                likeComment = (LikeComment) this.getLikeCommentById(id).getData();
            }
            if (likeComment == null) {
                likeComment = query().eq("comment_id", commentId)
                        .eq("user_id", userId).one();
            }
            if (likeComment == null) {
                return Result.fail("该点赞评论记录不存在");
            }
            if (id == null) {
                hashRedisClient.hSet(key,"id", likeComment.getId(), CACHE_LIKECOMMENT_COMMENT_USER_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeComment);
        } catch (ParseException e) {
            return Result.fail("获取点赞记录失败");
        }
    }

    /**
     * 获取用户点赞记录
     * @param userId 用户ID
     * @return 包含点赞记录的Result对象
     */
    @Override
    public Result getLikeNotice(Integer userId) {
        List<LikeComment> likeNoteList = list(query().getWrapper().eq("user_id", userId));
        List<LikeCommentNotice> noticeList = new ArrayList<>();
        for (LikeComment likeComment : likeNoteList) {
            Object userData = userCenterClient.getUserById(likeComment.getUserId()).getData();
            User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
            Object noteCommentData = notesClient.getNoteCommentById(likeComment.getCommentId()).getData();
            NoteCommentDTO noteComment = BeanUtil.mapToBean((Map<?, ?>) noteCommentData, NoteCommentDTO.class, true);
            noticeList.add(new LikeCommentNotice(noteComment, user, likeComment.getLikeTime()));
        }
        noticeList.sort(((o1, o2) -> o2.getLikeTime().compareTo(o1.getLikeTime())));
        return Result.ok(noticeList);
    }

    /**
     * 删除点赞记录
     * @param id 点赞记录主键
     * @return 操作结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result removeLikeComment(Integer id) {
        LikeComment likeComment = (LikeComment) this.getLikeCommentById(id).getData();
        if (!this.removeById(id)) {
            throw new RuntimeException("删除点赞评论记录" + id + "失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_DELETE_QUEUE_ROUTING_KEY, id);
        hashRedisClient.delete(CACHE_LIKECOMMENT_COMMENT_USER_KEY + likeComment.getCommentId() + ":" + likeComment.getUserId());
        return Result.ok();
    }

    /**
     * 创建新的点赞记录
     * @param likeComment 点赞实体对象
     * @return 操作结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result addLikeComment(LikeComment likeComment) {
        likeComment.setLikeTime(new Timestamp(System.currentTimeMillis()));
        if (!this.save(likeComment)) {
            throw new RuntimeException("添加新的点赞评论记录失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKECOMMENT_CACHE_ADD_QUEUE_ROUTING_KEY, likeComment);
        hashRedisClient.hSet(CACHE_LIKECOMMENT_COMMENT_USER_KEY + likeComment.getCommentId() + ":" + likeComment.getUserId()
                ,"id", likeComment.getId(), CACHE_LIKECOMMENT_COMMENT_USER_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 数据库回源查询方法
     * @param commentId 目标评论ID
     * @return 点赞记录集合
     */
    private List<LikeComment> getLikeCommentsFromDBForCommentId(Integer commentId) {
        List<LikeComment> likeCommentList = list(new QueryWrapper<LikeComment>().eq("comment_id", commentId));
        if (likeCommentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return likeCommentList;
    }
}
