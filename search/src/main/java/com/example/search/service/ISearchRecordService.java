package com.example.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.SearchRecord;

public interface ISearchRecordService extends IService<SearchRecord> {
    Result getSearchRecordById(Integer id);
    Result getSearchRecordsByUserId(Integer userId);
    Result removeSearchRecordsByUserId(Integer userId);
    Result removeSearchRecord(Integer id);
    Result addSearchRecord(SearchRecord searchRecord);
}
