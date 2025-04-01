package com.example.messages.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.ReplyCommentDTO;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeReply;
import com.example.littleredbook.entity.ReplyComment;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.messages.dto.LikeReplyNotice;
import com.example.messages.mapper.LikeReplyMapper;
import com.example.messages.service.ILikeReplyService;
import com.example.messages.utils.NotesClient;
import com.example.messages.utils.UserCenterClient;
import jakarta.annotation.Resource;
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
 * 回复点赞服务实现类
 *
 * <p>功能说明：
 * 1. 实现回复点赞关系核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存点赞记录<br>
 * 4. 设计复合键二级索引优化联合查询<br>
 * 5. 事务注解保障数据操作原子性<br>
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
public class LikeReplyServiceImpl extends ServiceImpl<LikeReplyMapper, LikeReply> implements ILikeReplyService {
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
    public Result getLikeReplyById(Integer id) {
        try {
            LikeReply likeReply = hashRedisClient.hMultiGet(CACHE_LIKEREPLY_KEY + id, LikeReply.class);
            if (likeReply == null) {
                likeReply = getById(id);
                if (likeReply == null) {
                    return Result.fail("点赞回复记录不存在");
                }
                hashRedisClient.hSet(CACHE_LIKEREPLY_REPLY_USER_KEY + likeReply.getReplyId() + ":" + likeReply.getUserId()
                        ,"id", likeReply.getId(), CACHE_LIKEREPLY_REPLY_USER_TTL, TimeUnit.MINUTES);
                mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_ADD_QUEUE_ROUTING_KEY, likeReply);
            }
            return Result.ok(likeReply);
        } catch (ParseException e) {
            return Result.fail("获取点赞回复记录ID为" + id + "失败");
        }
    }

    /**
     * 获取回复所有点赞记录
     * @param replyId 目标回复ID
     * @return 包含点赞集合的Result对象
     */
    @Override
    public Result getLikeRepliesByReplyId(Integer replyId) {
        List<Integer> likeReplyListIds = listObjs(query().getWrapper().eq("reply_id", replyId).select("id"));
        List<LikeReply> likeReplyList = new ArrayList<>();
        for(Integer id : likeReplyListIds) {
            Result result = this.getLikeReplyById(id);
            if (result.getSuccess()) {
                likeReplyList.add((LikeReply) result.getData());
            }
        }
        return Result.ok(likeReplyList);
    }

    /**
     * 联合查询用户对回复的点赞状态
     * @param replyId 目标回复ID
     * @param userId 查询用户ID
     * @return 包含点赞记录的Result对象
     */
    // TODO 可以在Redis中设计LikeReply的二级索引，即通过replyId+userId -> id的映射关系，从而达到使用Redis优化查询的目的
    // TODO 本方法未处理缓存穿透和缓存击穿（待后续优化）
    @Override
    public Result getLikeReplyByReplyIdAndUserId(Integer replyId, Integer userId) {
        String key = CACHE_LIKEREPLY_REPLY_USER_KEY + replyId + ":" + userId;
        try{
            Integer id = hashRedisClient.hMultiGet(key, Integer.class);
            LikeReply likeReply = null;
            if (id != null) {
                likeReply = (LikeReply) this.getLikeReplyById(id).getData();
            }
            if (likeReply == null) {
                likeReply = query().eq("reply_id", replyId)
                        .eq("user_id", userId).one();
            }
            if (likeReply == null) {
                return Result.fail("该点赞回复评论记录不存在");
            }
            if (id == null) {
                hashRedisClient.hSet(key,"id", likeReply.getId(), CACHE_LIKEREPLY_REPLY_USER_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeReply);
        } catch (ParseException e) {
            return Result.fail("获取点赞记录失败");
        }
    }

    /**
     * 获取点赞通知
     * @param userId 用户ID
     * @return 包含点赞通知的Result对象
     */
    @Override
    public Result getLikeNotice(Integer userId) {
        List<LikeReply> likeNoteList = list(query().getWrapper().eq("user_id", userId));
        List<LikeReplyNotice> noticeList = new ArrayList<>();
        for (LikeReply likeReply : likeNoteList) {
            Object userData = userCenterClient.getUserById(likeReply.getUserId()).getData();
            User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
            Object replyData = notesClient.getReplyCommentById(likeReply.getReplyId()).getData();
            ReplyCommentDTO reply = BeanUtil.mapToBean((Map<?, ?>) replyData, ReplyCommentDTO.class, true);
            noticeList.add(new LikeReplyNotice(reply, user, likeReply.getLikeTime()));
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
    public Result removeLikeReply(Integer id) {
        LikeReply likeReply = (LikeReply) this.getLikeReplyById(id).getData();
        if (!this.removeById(id)) {
            throw new RuntimeException("删除点赞回复记录" + id + "失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_DELETE_QUEUE_ROUTING_KEY, id);
        hashRedisClient.delete(CACHE_LIKEREPLY_REPLY_USER_KEY + likeReply.getReplyId() + ":" + likeReply.getUserId());
        return Result.ok();
    }

    /**
     * 创建新的点赞记录
     * @param likeReply 点赞实体对象
     * @return 操作结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result addLikeReply(LikeReply likeReply) {
        likeReply.setLikeTime(new Timestamp(System.currentTimeMillis()));
        if (!this.save(likeReply)) {
            throw new RuntimeException("添加新的点赞回复记录失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKEREPLY_CACHE_ADD_QUEUE_ROUTING_KEY, likeReply);
        hashRedisClient.hSet(CACHE_LIKEREPLY_REPLY_USER_KEY + likeReply.getReplyId() + ":" + likeReply.getUserId()
                ,"id", likeReply.getId(), CACHE_LIKEREPLY_REPLY_USER_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 数据库回源查询方法
     * @param replyId 目标回复ID
     * @return 点赞记录集合
     */
    private List<LikeReply> getLikeRepliesFromDBForReplyId(Integer replyId) {
        List<LikeReply> likeReplyList = list(new QueryWrapper<LikeReply>().eq("reply_id", replyId));
        if (likeReplyList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return likeReplyList;
    }
}
