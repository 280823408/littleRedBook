package com.example.search.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.SearchRecord;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.MQClient;
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

import static com.example.littleredbook.utils.MQConstants.*;
import static com.example.littleredbook.utils.RedisConstants.*;
/**
 * 搜索记录服务实现类
 *
 * <p>功能说明：
 * 1. 实现用户搜索记录核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存单条搜索记录<br>
 * 4. 提供搜索记录查询、新增、批量删除等服务<br>
 * 5. 事务注解保障数据库操作原子性<br>
 *
 * <p>关键方法：
 * - ID/用户维度记录查询<br>
 * - 批量删除操作的缓存清理<br>
 * - 带互斥锁的列表缓存查询<br>
 * - 自动记录搜索时间戳<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
@Service
public class SearchRecordServiceImp extends ServiceImpl<SearchRecordMapper, SearchRecord> implements ISearchRecordService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    @Resource
    private MQClient mqClient;

    /**
     * 根据ID查询搜索记录详情
     * @param id 搜索记录唯一标识
     * @return 包含搜索实体或错误信息的Result对象
     */
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
            mqClient.sendMessage(TOPIC_SEARCH_EXCHANGE, TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_ADD_QUEUE_ROUTING_KEY, searchRecord);
            return Result.ok(searchRecord);
        } catch (ParseException e) {
            return Result.fail("获取搜索记录ID为" + id + "失败");
        }
    }

    /**
     * 获取用户所有搜索记录
     * @param userId 用户唯一标识
     * @return 包含搜索记录集合的Result对象
     */
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

    /**
     * 清空用户搜索记录
     * @param userId 用户唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeSearchRecordsByUserId(Integer userId) {
        List<SearchRecord> searchRecords = this.getSearchRecordFromDBForUserId(userId);
        for (SearchRecord searchRecord: searchRecords) {
            mqClient.sendMessage(TOPIC_SEARCH_EXCHANGE, TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_DELETE_QUEUE_ROUTING_KEY, searchRecord.getId());
            this.removeById(searchRecord.getId());
        }
        return Result.ok();
    }

    /**
     * 删除指定搜索记录
     * @param id 记录唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeSearchRecord(Integer id) {
        if (!this.removeById(id)) {
            throw new RuntimeException("删除浏览记录失败");
        }
        mqClient.sendMessage(TOPIC_SEARCH_EXCHANGE, TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_DELETE_QUEUE_ROUTING_KEY, id);
        return Result.ok();
    }

    /**
     * 创建新的搜索记录
     * @param searchRecord 搜索记录实体
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result addSearchRecord(SearchRecord searchRecord) {
        searchRecord.setSearchTime(new Timestamp(System.currentTimeMillis()));
        if (!this.save(searchRecord)) {
            throw new RuntimeException("添加新的搜索记录失败");
        }
        mqClient.sendMessage(TOPIC_SEARCH_EXCHANGE, TOPIC_SEARCH_EXCHANGE_WITH_SEARCH_SEARCHRECORD_CACHE_ADD_QUEUE_ROUTING_KEY, searchRecord);
        return Result.ok();
    }

    /**
     * 数据库回源方法（用户维度）
     * @param userId 用户唯一标识
     * @return 搜索记录空集合或数据集合
     */
    private List<SearchRecord> getSearchRecordFromDBForUserId(Integer userId) {
        List<SearchRecord> searchRecords = list(new QueryWrapper<SearchRecord>().eq("user_id", userId));
        if (searchRecords.isEmpty()) {
            return Collections.emptyList();
        }
        return searchRecords;
    }
}
