package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;
import com.example.littleredbook.entity.Tag;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.dto.NoteDTO;
import com.example.notes.mapper.NoteMapper;
import com.example.notes.service.INoteService;
import com.example.notes.utils.LoginClient;
import com.example.notes.utils.TagClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.example.littleredbook.utils.RedisConstants.*;

@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {
    @Resource
    private LoginClient loginClient;
    @Resource
    private TagClient tagClient;
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;

    @Override
    public Result getNoteById(Integer id) throws ParseException {
        NoteDTO noteDTO = hashRedisClient.queryWithMutex(
                CACHE_NOTE_KEY,
                id,
                NoteDTO.class,
                this::getNoteDTOFromDB,
                CACHE_NOTE_TTL,
                TimeUnit.MINUTES
        );
        if (noteDTO == null) {
            return Result.fail("该笔记不存在!");
        }
        return Result.ok(noteDTO);
    }

//    /**
//     * 根据笔记ID查询笔记(通过JSON字符串存储版本)
//     */
//    @Override
//    @Transactional
//    public Result getNoteById(Integer id) {
//        NoteDTO noteDTO = stringRedisClient.queryWithMutex(
//                CACHE_NOTE_KEY,
//                id,
//                NoteDTO.class,
//                this::getNoteDTOFromDB,
//                CACHE_NOTE_TTL,
//                TimeUnit.MINUTES
//        );
//        if (noteDTO == null) {
//            return Result.fail("该笔记不存在!");
//        }
//        return Result.ok(noteDTO);
//    }

    /**
     * 根据用户ID查询笔记(通过JSON字符串存储版本)
     * @param userId
     * @return
     */
    @Override
    @Transactional
    public Result getNotesByUserId(Integer userId) {
        List<NoteDTO> noteDTOS = stringRedisClient.queryListWithMutex(
                CACHE_NOTE_USER_KEY,
                userId,
                NoteDTO.class,
                this::getNoteDTOsFromDB,
                CACHE_NOTE_USER_TTL,
                TimeUnit.MINUTES
        );
        if (noteDTOS.isEmpty()) {
            return Result.fail("该用户没有笔记!");
        }
        return Result.ok(noteDTOS);
    }
    /**
     * 数据库回源函数：查询数据库并组装 NoteDTO
     */
    private NoteDTO getNoteDTOFromDB(Integer id) {
        Note note = getById(id);
        if (note == null) {
            return null;
        }
        Object userData = loginClient.getUserById(note.getUserId()).getData();
        Object tagData = tagClient.getTagsByNoteId(id).getData();
        User user = BeanUtil.mapToBean((Map<?, ?>) userData, User.class, true);
        List<Tag> tags = BeanUtil.copyToList((List<?>) tagData, Tag.class);
        if (user == null) {
            log.error("用户服务调用失败: noteId={" + id + "}, userId={" + note.getUserId() + "}");
            return null;
        }
        if (tags == null) {
            log.error("标签服务调用失败: noteId={" + id + "}");
            return null;
        }
        NoteDTO noteDTO = BeanUtil.copyProperties(note, NoteDTO.class);
        noteDTO.setUser(user);
        noteDTO.setTags(tags);
        return noteDTO;
    }
    /**
     * 数据库回源函数：查询数据库并组装 NoteDTOs
     */
    private List<NoteDTO> getNoteDTOsFromDB(Integer userId) {
        List<Note> notes = list(new QueryWrapper<Note>().eq("user_id", userId));
        if (notes.isEmpty()) {
            return Collections.emptyList();
        }
        Object data = loginClient.getUserById(userId).getData();
        User user = BeanUtil.mapToBean((Map<?, ?>) data, User.class, true);
        if (user == null) {
            log.error("用户服务调用失败");
            return null;
        }
        List<NoteDTO> noteDTOs = BeanUtil.copyToList(notes, NoteDTO.class);
        noteDTOs.forEach(noteDTO -> noteDTO.setUser(user));
        return noteDTOs;
    }
}
