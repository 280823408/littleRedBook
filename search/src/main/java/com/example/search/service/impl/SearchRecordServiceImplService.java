package com.example.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.BrowseRecord;
import com.example.littleredbook.entity.SearchRecord;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import com.example.search.mapper.SearchRecordMapper;
import com.example.search.service.ISearchRecordService;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.sql.Timestamp;
import java.text.ParseException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;
@Service
public class SearchRecordServiceImplService extends ServiceImpl<SearchRecordMapper, SearchRecord> implements ISearchRecordService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Override
    public Result getSearchRecordById(Integer id) {
        try {
            SearchRecord searchRecord = hashRedisClient.hMultiGet(CACHE_SEARCH_KEY + id, SearchRecord.class);
            if (searchRecord == null) {
                searchRecord = getById(id);
            }
            if (searchRecord == null) {
                return Result.fail("该搜索记录ID不存在");
            }
            hashRedisClient.hMultiSet(CACHE_SEARCH_KEY + id, BrowseRecord.class);
            hashRedisClient.expire(CACHE_SEARCH_KEY + id, CACHE_SEARCH_TTL, TimeUnit.MINUTES);
            return Result.ok(searchRecord);
        } catch (ParseException e) {
            return Result.fail("获取搜索记录ID为" + id + "失败");
        }
    }

    @Override
    public Result getSearchRecordsByUserId(Integer userId) {
        List<SearchRecord> searchRecords = stringRedisClient.queryListWithMutex(
                CACHE_SEARCH_USER_KEY,
                userId,
                SearchRecord.class,
                this::getSearchRecordFromDBForUserId,
                CACHE_SEARCH_USER_TTL,
                TimeUnit.MINUTES
        );
        if (searchRecords == null) {
            return Result.fail("获取搜索记录列表失败");
        }
        return Result.ok(searchRecords);
    }

    @Override
    @Transactional
    public Result removeSearchRecordsByUserId(Integer userId) {
        List<SearchRecord> searchRecords = this.getSearchRecordFromDBForUserId(userId);
        for (SearchRecord searchRecord: searchRecords) {
            hashRedisClient.delete(CACHE_SEARCH_KEY + searchRecord.getId());
            this.removeById(searchRecord.getId());
        }
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeSearchRecord(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除浏览记录失败");
        }
        hashRedisClient.delete(CACHE_SEARCH_KEY + id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addSearchRecord(SearchRecord searchRecord) {
        searchRecord.setSearchTime(new Timestamp(System.currentTimeMillis()));
        if (!this.save(searchRecord)) {
            throw new RuntimeException("添加新的搜索记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_SEARCH_KEY + searchRecord.getId(), searchRecord);
        return Result.ok();
    }

    /**
     * 数据库回源方法
     * @param userId 用户id
     * @return 搜索记录列表
     */
    private List<SearchRecord> getSearchRecordFromDBForUserId(Integer userId) {
        List<SearchRecord> searchRecords = list(new QueryWrapper<SearchRecord>().eq("user_id", userId));
        if (searchRecords.isEmpty()) {
            return Collections.emptyList();
        }
        return searchRecords;
    }
}
