package com.example.messages.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;
import com.example.messages.service.ILikeNoteService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("likeNote")
public class LikeNoteController {
    @Resource
    private ILikeNoteService likeNoteService;
    @GetMapping("getLikeNoteById")
    public Result getLikeNoteById(@RequestParam Integer id) {
        return likeNoteService.getLikeNoteById(id);
    }

    @GetMapping("getLikeNotesByNoteId")
    public Result getLikeNotesByNoteId(@RequestParam Integer noteId) {
        return likeNoteService.getLikeNotesByNoteId(noteId);
    }

    @GetMapping("removeLikeNote")
    public Result removeLikeNote(@RequestParam Integer id) {
        return likeNoteService.removeLikeNote(id);
    }

    @PostMapping("addLikeNote")
    public Result addLikeNote(@RequestBody LikeNote likeNote) {
        return likeNoteService.addLikeNote(likeNote);
    }
}
