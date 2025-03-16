package com.example.search.controller;

import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.SearchRecord;
import com.example.search.service.ISearchRecordService;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户搜索记录控制器
 *
 * <p>功能说明：
 * 1. 处理用户搜索记录相关HTTP请求入口<br>
 * 2. 提供搜索记录的增删查等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 搜索记录单点查询<br>
 *   - 用户历史搜索记录获取<br>
 *   - 单条记录删除操作<br>
 *   - 用户全部搜索记录清除<br>
 *   - 新增搜索记录创建<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("searchRecord")
public class SearchRecordController {
    @Resource
    private ISearchRecordService searchRecordService;

    /**
     * 获取指定ID的搜索记录
     * @param id 搜索记录唯一标识
     * @return 包含搜索记录实体或错误信息的Result对象
     */
    @GetMapping("getSearchRecordById")
    public Result getSearchRecordById(@RequestParam Integer id) {
        return searchRecordService.getSearchRecordById(id);
    }

    /**
     * 获取用户所有历史搜索记录
     * @param userId 用户唯一标识
     * @return 包含搜索记录集合的Result对象
     */
    @GetMapping("getSearchRecordsByUserId")
    public Result getSearchRecordsByUserId(@RequestParam Integer userId) {
        return searchRecordService.getSearchRecordsByUserId(userId);
    }

    /**
     * 删除单条搜索记录
     * @param id 要删除的记录ID
     * @return 操作结果的Result对象
     */
    @GetMapping("removeSearchRecord")
    public Result removeSearchRecord(@RequestParam Integer id) {
        return searchRecordService.removeSearchRecord(id);
    }

    /**
     * 清除用户全部搜索记录
     * @param userId 要清除记录的用户ID
     * @return 操作结果的Result对象
     */
    @GetMapping("removeSearchRecordsByUserId")
    public Result removeSearchRecordsByUserId(@RequestParam Integer userId) {
        return searchRecordService.removeSearchRecordsByUserId(userId);
    }

    /**
     * 创建新的搜索记录
     * @param searchRecord 包含搜索内容的数据传输对象
     * @return 包含新增记录ID的Result对象
     */
    @PostMapping("addSearchRecord")
    public Result addSearchRecord(@RequestBody SearchRecord searchRecord) {
        return searchRecordService.addSearchRecord(searchRecord);
    }
}
