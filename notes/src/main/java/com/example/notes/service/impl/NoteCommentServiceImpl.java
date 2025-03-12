package com.example.notes.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.mapper.NoteCommentMapper;
import com.example.notes.service.INoteCommentService;
import com.example.notes.utils.LikeCommentClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements INoteCommentService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private LikeCommentClient likeCommentClient;
    @Override
    public Result getNoteCommentById(Integer id) {
        try {
            NoteComment comment = hashRedisClient.hMultiGet(CACHE_COMMENT_KEY + id, NoteComment.class);
            if (comment == null) {
                comment = getById(id);
                if (comment == null) {
                    return Result.fail("评论不存在");
                }
                hashRedisClient.hMultiSet(CACHE_COMMENT_KEY + id, comment);
                hashRedisClient.expire(CACHE_COMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
            }
            return Result.ok(comment);
        } catch (ParseException e) {
            return Result.fail("获取评论ID为" + id + "失败");
        }
    }

    @Override
    public Result getNoteCommentsByNoteId(Integer noteId) {
        List<NoteComment> comments = stringRedisClient.queryListWithMutex(
                CACHE_COMMENT_NOTE_KEY,
                noteId,
                NoteComment.class,
                this::getCommentsByNoteIdFromDB,
                CACHE_NOTE_COMMENTS_TTL,
                TimeUnit.MINUTES
        );
        if (comments == null) {
            return Result.fail("获取评论列表失败");
        }
        return Result.ok(comments);
    }

    @Override
    @Transactional
    public Result addNoteComment(NoteComment noteComment) {
        if (!this.save(noteComment)) {
            throw new RuntimeException("添加评论失败");
        }
        hashRedisClient.hMultiSet(CACHE_COMMENT_KEY + noteComment.getId(), noteComment);
        hashRedisClient.expire(CACHE_COMMENT_KEY + noteComment.getId(), CACHE_COMMENT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeNoteComment(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除评论失败");
        }
        hashRedisClient.delete(CACHE_COMMENT_KEY + id);
        return Result.ok();
    }

    /**
     * 规范代码示范
     * @param noteComment noteComment实体
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    @Transactional
    public Result likeNoteComment(NoteComment noteComment, Integer userId) {
        String sql = "like_num = like_num + 1";
        int delta = 1;
        LikeComment likeComment = (LikeComment) likeCommentClient.getLikeCommentByCommentIdAndUserId(
                noteComment.getId(), userId).getData();
        if (likeComment != null) {
            sql = "like_num = like_num - 1";
            delta = -1;
        }
        if (!update(new LambdaUpdateWrapper<NoteComment>()
                .eq(NoteComment::getId, noteComment.getId())
                .setSql(sql))) {
            throw new RuntimeException("点赞该评论失败");
        }
        if (likeComment != null) {
            likeCommentClient.removeLikeComment(likeComment.getId());
            hashRedisClient.hIncrement(CACHE_COMMENT_KEY + noteComment.getId(), "likeNum", delta);
            return Result.ok();
        }
        likeComment = new LikeComment();
        likeComment.setCommentId(noteComment.getId());
        likeComment.setUserId(userId);
        likeCommentClient.addLikeComment(likeComment);
        hashRedisClient.hIncrement(CACHE_COMMENT_KEY + noteComment.getId(), "likeNum", delta);
        return Result.ok();
    }

    /**
     * 从数据库查询指定笔记的评论列表（按时间倒序）
     */
    private List<NoteComment> getCommentsByNoteIdFromDB(Integer noteId) {
        List<NoteComment> commentList = query().eq("note_id", noteId)
                .orderByDesc("comment_time")
                .list();
        if (commentList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return commentList;
    }
}
