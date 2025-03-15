package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeComment;
import com.example.littleredbook.entity.NoteComment;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.mapper.NoteCommentMapper;
import com.example.notes.service.INoteCommentService;
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

@Service
public class NoteCommentServiceImpl extends ServiceImpl<NoteCommentMapper, NoteComment> implements INoteCommentService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;
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
                CACHE_COMMENT_NOTE_TTL,
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
     * @param id 评论ID
     * @param userId 用户ID
     * @return 操作结果
     */
    @Override
    public Result likeNoteComment(Integer id, Integer userId) {
        Object likeCommentData =  messagesClient.getLikeCommentByCommentIdAndUserId(
                id, userId).getData();
        LikeComment likeComment = BeanUtil.mapToBean((Map<?, ?>) likeCommentData, LikeComment.class, true);
        boolean isLike = likeComment.getId() != null;
        INoteCommentService noteCommentService = (INoteCommentService) AopContext.currentProxy();
        noteCommentService.updateNoteCommentLikeNum(id, isLike);
        if (isLike) {
            messagesClient.removeLikeComment(likeComment.getId());
            hashRedisClient.hIncrement(CACHE_COMMENT_KEY + id, "likeNum", -1);
            hashRedisClient.expire(CACHE_COMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
            return Result.ok();
        }
        likeComment.setCommentId(id);
        likeComment.setUserId(userId);
        messagesClient.addLikeComment(likeComment);
        hashRedisClient.hIncrement(CACHE_COMMENT_KEY + id, "likeNum", 1);
        hashRedisClient.expire(CACHE_COMMENT_KEY + id, CACHE_COMMENT_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result updateNoteCommentLikeNum(Integer id, boolean isLike) {
        String sql = isLike ? "like_num = like_num - 1" : "like_num = like_num + 1";
        if (!update(new LambdaUpdateWrapper<NoteComment>()
                .eq(NoteComment::getId, id)
                .setSql(sql))) {
            throw new RuntimeException("更新评论点赞数失败");
        }
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
