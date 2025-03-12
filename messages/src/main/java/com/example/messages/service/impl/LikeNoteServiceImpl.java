package com.example.messages.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.messages.mapper.LikeNoteMapper;
import com.example.messages.service.ILikeNoteService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class LikeNoteServiceImpl extends ServiceImpl<LikeNoteMapper, LikeNote> implements ILikeNoteService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Override
    public Result getLikeNoteById(Integer id) {
        try {
            LikeNote likeNote = hashRedisClient.hMultiGet(CACHE_LIKENOTE_KEY + id, LikeNote.class);
            if (likeNote == null) {
                likeNote = getById(id);
                if (likeNote == null) {
                    return Result.fail("点赞笔记记录不存在");
                }
                hashRedisClient.hMultiSet(CACHE_LIKENOTE_KEY + id, likeNote);
                hashRedisClient.expire(CACHE_LIKENOTE_KEY + id, CACHE_LIKENOTE_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeNote);
        } catch (ParseException e) {
            return Result.fail("获取点赞笔记记录ID为" + id + "失败");
        }
    }

    @Override
    public Result getLikeNotesByNoteId(Integer noteId) {
        List<LikeNote> likeNoteList = stringRedisClient.queryListWithMutex(
                CACHE_LIKENOTE_NOTE_KEY,
                noteId,
                LikeNote.class,
                this::getLikeNotesFromDBForNoteId,
                CACHE_LIKENOTE_NOTE_TTL,
                TimeUnit.MINUTES
        );
        if (likeNoteList == null) {
            return Result.fail("获取点赞笔记记录列表失败");
        }
        return Result.ok(likeNoteList);
    }

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
                likeNote = query().select("id").eq("note_id", noteId)
                        .eq("user_id", userId).one();
            }
            if (likeNote == null) {
                return Result.fail("该点赞笔记记录不存在");
            }
            if (id == null) {
                hashRedisClient.hMultiSet(key, likeNote.getId());
                hashRedisClient.expire(key,
                        CACHE_LIKENOTE_NOTE_USER_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(likeNote);
        } catch (ParseException e) {
            return Result.fail("获取点赞记录失败");
        }
    }

    @Override
    @Transactional
    public Result removeLikeNote(Integer id) {
        LikeNote likeNote = (LikeNote) this.getLikeNoteById(id).getData();
        if (!this.removeById(id)) {
            throw new RuntimeException("删除点赞笔记记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_LIKENOTE_KEY + id);
        hashRedisClient.delete(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + ":" + likeNote.getUserId());
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addLikeNote(LikeNote likeNote) {
        if (!this.save(likeNote)) {
            throw new RuntimeException("添加新的点赞笔记记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_LIKENOTE_KEY + likeNote.getId(), likeNote);
        hashRedisClient.expire(CACHE_LIKENOTE_KEY + likeNote.getId(), CACHE_LIKENOTE_TTL, TimeUnit.MINUTES);
        hashRedisClient.hMultiSet(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + " " + likeNote.getUserId(),
                likeNote);
        hashRedisClient.expire(CACHE_LIKENOTE_NOTE_USER_KEY + likeNote.getNoteId() + " " + likeNote.getUserId(),
                CACHE_LIKENOTE_NOTE_USER_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    /**
     * 从数据库查询指定笔记的点赞记录（数据库回源方法）
     */
    private List<LikeNote> getLikeNotesFromDBForNoteId(Integer noteId) {
        List<LikeNote> likeNoteList = list(new QueryWrapper<LikeNote>().eq("note_id", noteId));
        if (likeNoteList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return likeNoteList;
    }
}
