package com.example.messages.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.LikeNote;

public interface ILikeNoteService extends IService<LikeNote> {
    Result getLikeNoteById(Integer id);
    Result getLikeNotesByNoteId(Integer noteId);
    Result removeLikeNote(Integer id);
    Result addLikeNote(LikeNote likeNote);
}
