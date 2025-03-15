package com.example.notes.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;
import com.example.notes.service.INoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.text.ParseException;

/**
 * 笔记功能控制器
 *
 * <p>功能说明：
 * 1. 处理笔记相关HTTP请求入口<br>
 * 2. 提供笔记增删改查、排序、标签查询等核心功能<br>
 * 3. 集成Redis缓存提升热点数据访问性能<br>
 * 4. 支持跨域请求访问（@CrossOrigin）<br>
 * 5. 统一返回Result标准响应格式<br>
 *
 * <p>主要接口：
 * - 按ID/用户ID/标题精确查询笔记<br>
 * - 按点赞量/创建时间全局排序<br>
 * - 按标签分类查询笔记<br>
 * - 笔记创建与更新操作<br>
 *
 * @author Mike
 * @since 2025/3/2
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("notes")
public class NoteController {
    @Resource
    private INoteService noteService;

    /**
     * 根据笔记ID获取笔记详情
     * @param id 笔记唯一标识
     * @return Result对象，包含笔记详情或错误信息
     * @throws ParseException 当日期解析异常时抛出
     */
    @GetMapping("getNoteById")
    public Result getNoteById(@RequestParam Integer id) throws ParseException {
        return noteService.getNoteById(id);
    }

    /**
     * 查询指定用户的所有笔记
     * @param userId 用户ID
     * @return Result对象，包含笔记列表或错误信息
     */
    @GetMapping("getNotesByUserId")
    public Result getNotesByUserId(@RequestParam Integer userId) {
        return noteService.getNotesByUserId(userId);
    }

    /**
     * 根据标题关键词搜索笔记
     * @param title 笔记标题关键词
     * @return Result对象，包含匹配的笔记列表或错误信息
     */
    @GetMapping("getNotesByTitle")
    public Result getNotesByTitle(@RequestParam String title) {
        return noteService.getNotesByTitle(title);
    }

    /**
     * 获取全站笔记按点赞量排序
     * @param userId 当前用户ID（用于个性化显示）
     * @return Result对象，包含排序后的笔记列表或错误信息
     */
    @GetMapping("getAllNotesSortedByLikeNum")
    public Result getAllNotesSortedByLikeNum(@RequestParam Integer userId) {
        return noteService.getAllNotesSortedByLikeNum(userId);
    }

    /**
     * 获取全站笔记按创建时间排序
     * @param userId 当前用户ID（用于访问控制）
     * @return Result对象，包含按时间排序的笔记列表或错误信息
     */
    @GetMapping("getAllNotesSortedByCreatTime")
    public Result getAllNotesSortedByCreatTime(@RequestParam Integer userId) {
        return noteService.getAllNotesSortedByCreatTime(userId);
    }

    /**
     * 根据标签ID查询关联笔记
     * @param tagId 标签唯一标识
     * @return Result对象，包含该标签下的笔记集合或错误信息
     */
    @GetMapping("getNotesByTag")
    public Result getNotesByTag(@RequestParam Integer tagId) {
        return noteService.getNotesByTag(tagId);
    }

    /**
     * 创建新笔记
     * @param note 笔记实体对象（JSON格式）
     * @return Result对象，包含新建笔记ID或错误信息
     */
    @PostMapping("addNote")
    public Result addNote(@RequestBody Note note) {
        return noteService.addNote(note);
    }

    /**
     * 更新已有笔记
     * @param note 笔记实体对象（需包含ID）
     * @return Result对象，包含更新状态或错误信息
     */
    @PostMapping("updateNote")
    public Result updateNote(@RequestBody Note note) {
        return noteService.updateNote(note);
    }

    /**
     * 点赞/取消点赞笔记
     * @param id 笔记ID（用于点赞记录）
     * @param userId 当前用户ID（用于点赞记录）
     * @return Result对象，包含点赞操作结果或错误信息
     */
    @GetMapping("likeNote")
    public Result likeNote(@RequestParam Integer id, @RequestParam Integer userId) {
        return noteService.likeNote(id, userId);
    }
//    /**
//     * 测试hash结构的redis存储
//     */
//    @PostMapping("test")
//    public Result test() throws ParseException {
//        User user = new User();
//        user.setId(1);
//        user.setUserName("test");
//        user.setUserPassword("123456");
//        user.setPhone("12345678901");
//        NoteDTO note = new NoteDTO();
//        note.setId(1);
//        note.setUser(user);
//        note.setTitle("test");
//        note.setContent("test");
//        List<Tag> tags = new ArrayList<>();
//        tags.add(new Tag(1,"11"));
//        note.setTags(tags);
//        note.setCreateTime(Timestamp.valueOf(LocalDateTime.now()));
//        hashRedisClient.hMultiSet("test",note);
//        NoteDTO note1 = hashRedisClient.hMultiGet("test", NoteDTO.class);
//        log.debug(note1.toString());
//        return Result.ok(note1);
//    }

}
