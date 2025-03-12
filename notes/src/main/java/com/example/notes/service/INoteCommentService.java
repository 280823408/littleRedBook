package com.example.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.NoteComment;

public interface INoteCommentService extends IService<NoteComment> {
    Result getNoteCommentById(Integer id);
    Result getNoteCommentsByNoteId(Integer noteId);
    Result addNoteComment(NoteComment noteComment);
    Result removeNoteComment(Integer id);
    Result likeNoteComment(NoteComment noteComment, Integer userId);
}
