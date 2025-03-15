package com.example.search.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.SearchRecord;
import com.example.search.service.ISearchRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("searchRecord")
public class SearchRecordController {
    @Resource
    private ISearchRecordService searchRecordService;

    @GetMapping("getSearchRecordById")
    public Result getSearchRecordById(@RequestParam Integer id) {
        return searchRecordService.getSearchRecordById(id);
    }

    @GetMapping("getSearchRecordsByUserId")
    public Result getSearchRecordsByUserId(@RequestParam Integer userId) {
        return searchRecordService.getSearchRecordsByUserId(userId);
    }

    @GetMapping("removeSearchRecord")
    public Result removeSearchRecord(@RequestParam Integer id) {
        return searchRecordService.removeSearchRecord(id);
    }

    @GetMapping("removeSearchRecordsByUserId")
    public Result removeSearchRecordsByUserId(@RequestParam Integer userId) {
        return searchRecordService.removeSearchRecordsByUserId(userId);
    }

    @PostMapping("addSearchRecord")
    public Result addSearchRecord(@RequestBody SearchRecord searchRecord) {
        return searchRecordService.addSearchRecord(searchRecord);
    }
}
