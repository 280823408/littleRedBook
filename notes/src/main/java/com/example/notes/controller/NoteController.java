package com.example.notes.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;
import com.example.littleredbook.entity.Tag;
import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.notes.dto.NoteDTO;
import com.example.notes.service.INoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Field;
import java.sql.Timestamp;
import java.text.ParseException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("notes")
public class NoteController {
    @Resource
    private INoteService noteService;
    @Resource
    private HashRedisClient hashRedisClient;
    @PostMapping("getNoteById")
    public Result getNoteById(@RequestParam Integer id) throws ParseException {
        return noteService.getNoteById(id);
    }
    @PostMapping("getNotesByUserId")
    public Result getNotesByUserId(@RequestParam Integer userId) {
        return noteService.getNotesByUserId(userId);
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
