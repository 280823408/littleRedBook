package com.example.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Concern;

public interface IConcernService extends IService<Concern> {
    Result getConcernById(Integer id);
    Result getConcernByUserId(Integer userId);
    Result getConcernNumByUserId(Integer userId);
    Result removeConcernById(Integer id);
    Result removeConcernByUserIdAndFansId(Integer userId, Integer fansId);
    Result addConcern(Concern concern);
}
