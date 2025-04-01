package com.example.messages.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.NoteDTO;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.*;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.messages.dto.LikeNoteNotice;
import com.example.messages.mapper.LikeNoteMapper;
import com.example.messages.service.ILikeNoteService;
import com.example.messages.utils.NotesClient;
import com.example.messages.utils.UserCenterClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.*;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.*;
/**
 * 笔记点赞服务实现类
 *
 * <p>功能说明：
 * 1. 实现笔记点赞关系核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存点赞记录<br>
 * 4. 设计复合键二级索引优化联合查询<br>
 * 5. 事务注解保障数据操作原子性<br>
 *
 * <p>关键特性：
 * - 主记录缓存与二级索引同步维护<br>
 * - 带互斥锁的列表缓存查询机制<br>
 * - 双删策略保障缓存一致性<br>
 * - 自动记录点赞时间戳<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
@Service
public class LikeNoteServiceImpl extends ServiceImpl<LikeNoteMapper, LikeNote> implements ILikeNoteService {
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
    public Result getLikeNoteById(Integer id) {
        try {
            LikeNote likeNote = hashRedisClient.hMultiGet(CACHE_LIKENOTE_KEY + id, LikeNote.class);
            if (likeNote != null) {
                return Result.ok(likeNote);
            }
            likeNote = getById(id);
            if (likeNote == null) {
                return Result.fail("点赞笔记记录不存在");
            }
            hashRedisClient.hSet(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + ":" + likeNote.getUserId()
                    ,"id", likeNote.getId(), CACHE_LIKENOTE_NOTE_USER_TTL, TimeUnit.MINUTES);
            mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_ADD_QUEUE_ROUTING_KEY, likeNote);
            return Result.ok(likeNote);
        } catch (ParseException e) {
            return Result.fail("获取点赞笔记记录ID为" + id + "失败");
        }
    }

    /**
     * 获取笔记所有点赞记录
     * @param noteId 目标笔记ID
     * @return 包含点赞集合的Result对象
     */
    @Override
    public Result getLikeNotesByNoteId(Integer noteId) {
        List<Integer> likeNoteListIds = listObjs(query().getWrapper().eq("note_id", noteId).select("id"));
        List<LikeNote> likeNoteList = new ArrayList<>();
        for(Integer id : likeNoteListIds) {
            Result result = this.getLikeNoteById(id);
            if (result.getSuccess()) {
                likeNoteList.add((LikeNote) result.getData());
            }
        }
        return Result.ok(likeNoteList);
    }

    /**
     * 获取用户所有点赞记录
     * @param userId 目标用户ID
     * @return 包含点赞集合的Result对象
     */
    @Override
    public Result getLikesNotesByUserId(Integer userId) {
        List<LikeNote> likeNoteList = list(query().getWrapper().eq("user_id", userId));
        Set<Integer> noteIdList = new HashSet<>();
        for (LikeNote likeNote : likeNoteList) {
            noteIdList.add(likeNote.getNoteId());
        }
        return Result.ok(new ArrayList<>(noteIdList));
    }


    /**
     * 联合查询用户对笔记的点赞状态
     * @param noteId 目标笔记ID
     * @param userId 查询用户ID
     * @return 包含点赞记录的Result对象
     */
    // TODO 可以在Redis中设计LikeNote的二级索引，即通过noteId+userId -> id的映射关系，从而达到使用Redis优化查询的目的
    // TODO 本方法未处理缓存穿透和缓存击穿（待后续优化）
    @Override
    public Result getLikeNoteByNoteIdAndUserId(Integer noteId, Integer userId) {
        String key = CACHE_LIKENOTE_NOTE_USER_KEY + noteId + ":" + userId;
        try{
            Integer id = hashRedisClient.hMultiGet(key, Integer.class);
            LikeNote likeNote = null;
            if (id != null) {
                likeNote = (LikeNote) this.getLikeNoteById(id).getData();
            }
            if (likeNote == null) {
                likeNote = query().eq("note_id", noteId)
                        .eq("user_id", userId).one();
            }
            if (likeNote == null) {
                return Result.fail("该点赞笔记记录不存在");
            }
            if (id == null) {
                hashRedisClient.hSet(key,"id", likeNote.getId(), CACHE_LIKENOTE_NOTE_USER_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeNote);
        } catch (ParseException e) {
            return Result.fail("获取点赞记录失败");
        }
    }

    /**
     * 获取点赞通知
     * @param userId 目标用户ID
     * @return 包含点赞通知的Result对象
     */
    @Override
    public Result getLikeNotice(Integer userId) {
        List<LikeNote> likeNoteList = list(query().getWrapper().eq("user_id", userId));
        List<LikeNoteNotice> noticeList = new ArrayList<>();
        for (LikeNote likeNote : likeNoteList) {
            Object userData = userCenterClient.getUserById(likeNote.getUserId()).getData();
            User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
            Object noteCommentData = null;
            try {
                noteCommentData = notesClient.getNoteById(likeNote.getNoteId()).getData();
            } catch (ParseException e) {
                e.printStackTrace();
            }
            NoteDTO note = BeanUtil.mapToBean((Map<?, ?>) noteCommentData, NoteDTO.class, true);
            noticeList.add(new LikeNoteNotice(note, user, likeNote.getLikeTime()));
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
    public Result removeLikeNote(Integer id) {
        LikeNote likeNote = (LikeNote) this.getLikeNoteById(id).getData();
        if (!this.removeById(id)) {
            throw new RuntimeException("删除点赞笔记记录" + id + "失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_DELETE_QUEUE_ROUTING_KEY, likeNote.getId());
        hashRedisClient.delete(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + ":" + likeNote.getUserId());
        return Result.ok();
    }

    /**
     * 创建新的点赞记录
     * @param likeNote 点赞实体对象
     * @return 操作结果
     */
    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result addLikeNote(LikeNote likeNote) {
        likeNote.setLikeTime(new Timestamp(System.currentTimeMillis()));
        if (!this.save(likeNote)) {
            throw new RuntimeException("添加新的点赞笔记记录失败");
        }
        mqClient.sendMessage(TOPIC_MESSAGES_EXCHANGE, TOPIC_MESSAGES_EXCHANGE_WITH_MESSAGES_LIKENOTE_CACHE_ADD_QUEUE_ROUTING_KEY, likeNote);
        hashRedisClient.hSet(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + ":" + likeNote.getUserId()
                ,"id", likeNote.getId(), CACHE_LIKENOTE_NOTE_USER_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 数据库回源查询方法
     * @param noteId 目标笔记ID
     * @return 点赞记录集合
     */
    private List<LikeNote> getLikeNotesFromDBForNoteId(Integer noteId) {
        List<LikeNote> likeNoteList = list(new QueryWrapper<LikeNote>().eq("note_id", noteId));
        if (likeNoteList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return likeNoteList;
    }
}
