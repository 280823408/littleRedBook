package com.example.messages.utils;

import com.example.littleredbook.config.FeignConfiguration;
import com.example.littleredbook.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.text.ParseException;

@FeignClient(name = "notes", url = "http://localhost:8103", configuration = FeignConfiguration.class)
public interface NotesClient {
    @GetMapping("/note/authors/{authorId}/notes")
    Result getNoteIdsByAuthorId(@PathVariable Integer authorId);
    @GetMapping("/note/{id}")
    public Result getNoteById(@PathVariable Integer id) throws ParseException;
    @GetMapping("/note-comments/{id}")
    public Result getNoteCommentById(@PathVariable Integer id);
    @GetMapping("/reply-comments/{id}")
    public Result getReplyCommentById(@PathVariable Integer id);
}
