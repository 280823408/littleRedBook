package com.example.community.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Concern;

/**
 * 关注服务接口
 *
 * <p>功能说明：
 * 1. 定义用户关注关系核心业务逻辑接口<br>
 * 2. 实现关注数据持久化操作及关联关系管理<br>
 * 3. 支持单关注查询、用户关注列表查询、关注数量统计等核心功能<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 继承IService提供基础CRUD能力<br>
 *
 * <p>主要方法：
 * - 根据ID查询单个关注关系<br>
 * - 根据用户ID查询其关注列表<br>
 * - 统计用户关注数量<br>
 * - 根据ID移除关注关系<br>
 * - 根据用户ID与粉丝ID双向移除关注<br>
 * - 新增关注关系实体记录<br>
 *
 * @author Mike
 * @since 2025/3/6
 */
public interface IConcernService extends IService<Concern> {
    /**
     * 根据关注关系ID查询详细信息
     * @param id 关注关系唯一标识
     * @return 包含关注实体或错误信息的Result对象
     */
    Result getConcernById(Integer id);

    /**
     * 查询指定用户的所有关注列表
     * @param userId 被关注用户唯一标识
     * @return 包含关注关系集合的Result对象
     */
    Result getConcernByUserId(Integer userId);

    /**
     * 统计用户关注数量
     * @param userId 目标用户唯一标识
     * @return 包含关注数量统计结果的Result对象
     */
    Result getConcernNumByUserId(Integer userId);

    /**
     * 根据ID移除关注关系
     * @param id 关注关系唯一标识
     * @return 删除操作结果的Result对象
     */
    Result removeConcernById(Integer id);

    /**
     * 根据用户与粉丝双向解除关注
     * @param userId 被关注用户ID
     * @param fansId 发起关注的粉丝用户ID
     * @return 关联关系解除操作结果的Result对象
     */
    Result removeConcernByUserIdAndFansId(Integer userId, Integer fansId);

    /**
     * 新增关注关系实体记录
     * @param concern 待新增的关注关系对象
     * @return 包含新增实体ID的Result对象
     */
    Result addConcern(Concern concern);
}
