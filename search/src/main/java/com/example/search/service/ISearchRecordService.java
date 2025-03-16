package com.example.search.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.SearchRecord;

/**
 * 搜索记录服务接口
 *
 * <p>功能说明：
 * 1. 定义用户搜索行为记录核心业务逻辑接口<br>
 * 2. 实现搜索记录的存储、检索与清理功能<br>
 * 3. 支持用户级搜索记录管理及历史数据维护<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单条搜索记录<br>
 * - 获取用户全部搜索历史记录<br>
 * - 清空用户所有搜索记录<br>
 * - 删除指定搜索记录条目<br>
 * - 新增用户搜索行为记录<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
public interface ISearchRecordService extends IService<SearchRecord> {
    /**
     * 根据记录ID查询搜索详情
     * @param id 搜索记录唯一标识
     * @return 包含搜索记录实体或错误信息的Result对象
     */
    Result getSearchRecordById(Integer id);

    /**
     * 获取用户全部搜索记录（默认按时间倒序）
     * @param userId 用户唯一标识
     * @return 包含搜索记录集合的Result对象
     */
    Result getSearchRecordsByUserId(Integer userId);

    /**
     * 清空用户所有搜索历史
     * @param userId 目标用户唯一标识
     * @return 包含删除数量的Result对象
     */
    Result removeSearchRecordsByUserId(Integer userId);

    /**
     * 删除单条搜索记录
     * @param id 记录唯一标识
     * @return 包含操作结果的Result对象
     */
    Result removeSearchRecord(Integer id);

    /**
     * 新增用户搜索记录
     * @param searchRecord 包含搜索内容、用户ID等字段的实体对象
     * @return 包含新增记录ID的Result对象
     */
    Result addSearchRecord(SearchRecord searchRecord);
}
