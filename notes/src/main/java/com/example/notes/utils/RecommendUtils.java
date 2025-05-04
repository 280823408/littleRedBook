package com.example.notes.utils;

import com.example.littleredbook.dto.NoteDTO;
import com.example.littleredbook.dto.Result;

import java.util.List;

public interface RecommendUtils {
    Result recommendNotes(Integer userId);
    Result recommendNotesByContent(Integer userId, String content);
}
