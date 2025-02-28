package com.example.notes.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Note;
import org.apache.ibatis.annotations.Select;


public interface NoteMapper extends BaseMapper<Note> {
}
