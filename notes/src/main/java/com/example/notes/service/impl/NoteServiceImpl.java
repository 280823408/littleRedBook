package com.example.notes.service.impl;

import cn.hutool.core.bean.BeanUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.*;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.notes.dto.NoteDTO;
import com.example.notes.mapper.NoteMapper;
import com.example.notes.service.INoteService;
import com.example.notes.utils.MessagesClient;
import com.example.notes.utils.UserCenterClient;
import com.example.notes.utils.CommunityClient;
import jakarta.annotation.Resource;
import org.springframework.aop.framework.AopContext;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;
import static com.example.littleredbook.utils.RedisConstants.CACHE_NOTELIST_TTL;

/**
 * 笔记服务实现类
 *
 * <p>功能说明：
 * 1. 实现笔记核心业务逻辑处理<br>
 * 2. 集成多级缓存架构（Hash结构+字符串结构）<br>
 * 3. 支持复杂查询的缓存穿透解决方案<br>
 * 4. 实现跨服务数据聚合（用户服务+标签服务）<br>
 * 5. 保证数据最终一致性（@Transactional）<br>
 *
 * <p>设计要点：
 * - 采用互斥锁解决缓存击穿问题<br>
 * - 使用Hutool BeanUtil实现安全对象转换<br>
 * - 支持多种排序规则的内存排序<br>
 * - 缓存更新采用延迟双删策略<br>
 *
 * @author Mike
 * @since 2025/3/1
 */
@Service
public class NoteServiceImpl extends ServiceImpl<NoteMapper, Note> implements INoteService {
    @Resource
    private UserCenterClient userCenterClient;
    @Resource
    private CommunityClient communityClient;
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MessagesClient messagesClient;
    @Resource
    private MQClient mqClient;
    /**
     * 根据笔记ID查询完整笔记信息
     * @param id 笔记唯一标识
     * @return Result标准响应，包含笔记详情或错误信息
     * @throws ParseException 当时间格式解析异常时抛出
     */
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
    public Result getNotesByUserId(Integer userId) {
        List<NoteDTO> noteDTOS = stringRedisClient.queryListWithMutex(
                CACHE_NOTE_USER_KEY,
                userId,
                NoteDTO.class,
                this::getNoteDTOsFromDBForUserId,
                CACHE_NOTE_USER_TTL,
                TimeUnit.MINUTES
        );
        if (noteDTOS.isEmpty()) {
            return Result.fail("该用户没有笔记!");
        }
        return Result.ok(noteDTOS);
    }

    /**
     * 根据标题关键词模糊查询笔记
     * @param title 搜索关键词
     * @return Result标准响应，包含匹配的笔记列表
     */
    @Override
    public Result getNotesByTitle(String title) {
        List<Integer> noteIds = listObjs(new QueryWrapper<Note>().select("id").like("title", title), obj -> (Integer) obj);
        List<NoteDTO> noteDTOS = new ArrayList<>();
        if (noteIds.isEmpty()) {
            return Result.fail("没有找到相关笔记!");
        }
        noteIds.forEach(id -> {
            try {
                noteDTOS.add(hashRedisClient.queryWithMutex(
                        CACHE_NOTELIST_KEY,
                        id,
                        NoteDTO.class,
                        this::getNoteDTOFromDB,
                        CACHE_NOTELIST_TTL,
                        TimeUnit.MINUTES
                ));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        return Result.ok(noteDTOS);
    }

    /**
     * 获取全站笔记并按点赞数降序排列
     * @param userId 当前用户ID（预留字段）
     * @return Result标准响应，包含排序后的笔记列表
     */
    @Override
    public Result getAllNotesSortedByLikeNum(Integer userId) {
        List<Integer> noteIds = listObjs(new QueryWrapper<Note>().select("id"), obj -> (Integer) obj);
        List<NoteDTO> noteDTOS = new ArrayList<>();
        if (noteIds.isEmpty()) {
            return Result.fail("没有找到相关笔记!");
        }
        noteIds.forEach(id -> {
            try {
                noteDTOS.add(hashRedisClient.queryWithMutex(
                        CACHE_NOTELIST_KEY,
                        id,
                        NoteDTO.class,
                        this::getNoteDTOFromDB,
                        CACHE_NOTELIST_TTL,
                        TimeUnit.MINUTES
                ));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        noteDTOS.sort((o1, o2) -> (int) (o2.getLikeNum() - o1.getLikeNum()));
        return Result.ok(noteDTOS);
    }

    /**
     * 获取全站笔记并按创建时间倒序排列
     * @param userId 当前用户ID（预留字段）
     * @return Result标准响应，包含时间排序的笔记列表
     */
    @Override
    public Result getAllNotesSortedByCreatTime(Integer userId) {
        List<Integer> noteIds = listObjs(new QueryWrapper<Note>().select("id"), obj -> (Integer) obj);
        List<NoteDTO> noteDTOS = new ArrayList<>();
        if (noteIds.isEmpty()) {
            return Result.fail("没有找到相关笔记!");
        }
        noteIds.forEach(id -> {
            try {
                noteDTOS.add(hashRedisClient.queryWithMutex(
                        CACHE_NOTELIST_KEY,
                        id,
                        NoteDTO.class,
                        this::getNoteDTOFromDB,
                        CACHE_NOTELIST_TTL,
                        TimeUnit.MINUTES
                ));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        noteDTOS.sort((o1, o2) -> o2.getCreateTime().compareTo(o1.getCreateTime()));
        return Result.ok(noteDTOS);
    }

    /**
     * 根据标签查询关联笔记
     * @param tagId 标签ID
     * @return Result标准响应，包含标签关联的笔记列表
     */
    @Override
    public Result getNotesByTag(Integer tagId) {
        Object tagData = communityClient.getNoteIdByTagId(tagId).getData();
        List<Integer> noteIds = BeanUtil.copyToList((List<?>) tagData, Integer.class);
        List<NoteDTO> noteDTOS = new ArrayList<>();
        if (noteIds.isEmpty()) {
            return Result.fail("没有找到相关笔记!");
        }
        noteIds.forEach(id -> {
            try {
                noteDTOS.add(hashRedisClient.queryWithMutex(
                        CACHE_NOTELIST_KEY,
                        id,
                        NoteDTO.class,
                        this::getNoteDTOFromDB,
                        CACHE_NOTELIST_TTL,
                        TimeUnit.MINUTES
                ));
            } catch (ParseException e) {
                throw new RuntimeException(e);
            }
        });
        noteDTOS.sort((o1, o2) -> (int) (o2.getLikeNum() - o1.getLikeNum()));
        return Result.ok(noteDTOS);
    }

    /**
     * 新增笔记（带缓存清理）
     * @param note 笔记实体
     * @return Result标准响应，包含操作结果
     */
    @Override
    @Transactional
    public Result addNote(Note note) {
        if (!this.updateById(note)) {
            throw new RuntimeException("添加新笔记失败");
        }
        hashRedisClient.delete(CACHE_NOTE_KEY + note.getId());
        return Result.ok();
    }

    /**
     * 更新笔记信息（带缓存失效）
     * @param note 包含更新字段的笔记实体
     * @return Result标准响应，包含操作结果
     */
    @Override
    @Transactional
    public Result updateNote(Note note) {
        Integer id = note.getId();
        if (id == null) {
            return Result.fail("笔记ID不能为空!");
        }
        if (!this.save(note)) {
            throw new RuntimeException("修改笔记失败");
        }
        hashRedisClient.delete(CACHE_NOTE_KEY + id);
        return Result.ok();
    }

    @Override
    public Result likeNote(Integer id, Integer userId) {
        Object likeNoteData =  messagesClient.getLikeNoteByNoteIdAndUserId(
                id, userId).getData();
        LikeNote likeNote = BeanUtil.mapToBean((Map<?, ?>) likeNoteData, LikeNote.class, true);
        boolean isLike = likeNote.getId() != null;
        INoteService noteService = (INoteService) AopContext.currentProxy();
        noteService.updateNoteLikeNum(id, isLike);
        if (isLike) {
            messagesClient.removeLikeNote(likeNote.getId());
            hashRedisClient.hIncrement(CACHE_NOTE_KEY + id, "likeNum", -1);
            hashRedisClient.expire(CACHE_NOTE_KEY + id, CACHE_NOTE_TTL, TimeUnit.MINUTES);
            return Result.ok();
        }
        likeNote.setNoteId(id);
        likeNote.setUserId(userId);
        messagesClient.addLikeNote(likeNote);
        hashRedisClient.hIncrement(CACHE_NOTE_KEY + id, "likeNum", 1);
        hashRedisClient.expire(CACHE_NOTE_KEY + id, CACHE_NOTE_TTL, TimeUnit.MINUTES);
        return Result.ok();
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public Result updateNoteLikeNum(Integer id, boolean isLike) {
        String sql = isLike ? "like_num = like_num - 1" : "like_num = like_num + 1";
        if (!update(new LambdaUpdateWrapper<Note>()
                .eq(Note::getId, id)
                .setSql(sql))) {
            throw new RuntimeException("更新评论点赞数失败");
        }
        return Result.ok();
    }

    /**
     * 数据库回源方法：组装完整笔记DTO
     * @param id 笔记ID
     * @return 组合用户信息和标签的完整DTO对象
     * @implNote 1. 调用用户服务获取用户信息 2. 调用标签服务获取标签数据
     */
    private NoteDTO getNoteDTOFromDB(Integer id) {
        Note note = getById(id);
        if (note == null) {
            return null;
        }
        Object userData = userCenterClient.getUserById(note.getUserId()).getData();
        Object tagData = communityClient.getTagsByNoteId(id).getData();
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
     * 数据库回源方法：批量获取用户笔记DTO
     * @param userId 用户ID
     * @return 包含用户所有笔记的DTO列表
     * @implNote 1. 批量查询优化 2. 统一用户信息填充
     */
    private List<NoteDTO> getNoteDTOsFromDBForUserId(Integer userId) {
        List<Note> notes = list(new QueryWrapper<Note>().eq("user_id", userId));
        if (notes.isEmpty()) {
            return Collections.emptyList();
        }
        Object data = userCenterClient.getUserById(userId).getData();
        User user = BeanUtil.mapToBean((Map<?, ?>) data, User.class, true);
        if (user == null) {
            log.error("用户服务调用失败");
            return null;
        }
        List<NoteDTO> noteDTOs = BeanUtil.copyToList(notes, NoteDTO.class);
        noteDTOs.forEach(noteDTO -> noteDTO.setUser(user));
        noteDTOs.forEach(noteDTO -> {
          Object tagData = communityClient.getTagsByNoteId(noteDTO.getId()).getData();
          List<Tag> tags = BeanUtil.copyToList((List<?>) tagData, Tag.class);
          if (tags == null) {
              log.error("标签服务调用失败: noteId={" + noteDTO.getId() + "}");
          }
          noteDTO.setTags(tags);
        });
        return noteDTOs;
    }
}
