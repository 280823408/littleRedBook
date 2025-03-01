package com.example.notes.utils;

import com.example.littleredbook.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "community",url = "http://localhost:8100", path = "/tag")
public interface TagClient {
    @PostMapping("getTagsByNoteId")
    public Result getTagsByNoteId(@RequestParam Integer noteId);
    @PostMapping("getNoteIdByTagId")
    public Result getNoteIdByTagId(@RequestParam Integer tagId);
}
