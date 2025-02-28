package com.example.notes.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;

import java.text.ParseException;

public interface INoteService extends IService<Note> {
    Result getNoteById(Integer id) throws ParseException;
    Result getNotesByUserId(Integer userId);
}
