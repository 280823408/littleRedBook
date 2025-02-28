package com.example.notes.utils;

import com.example.littleredbook.dto.Result;

public class TagClientFallback implements TagClient {
    @Override
    public Result getTagsByNoteId(Integer noteId) {
        return Result.fail("标签服务不可用");
    }
}
