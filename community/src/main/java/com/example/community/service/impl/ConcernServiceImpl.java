package com.example.community.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.community.mapper.ConcernMapper;
import com.example.community.service.IConcernService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Concern;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

/**
 * 关注服务实现类
 *
 * <p>功能说明：
 * 1. 实现用户关注关系核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis Hash结构缓存单条关注记录<br>
 * 4. 提供关注查询、删除、新增等基础服务<br>
 * 5. 事务注解保障关注关系操作原子性<br>
 *
 * <p>关键方法：
 * - ID/用户维度关注记录查询<br>
 * - 关注量统计服务<br>
 * - 双删策略维护缓存一致性<br>
 * - 互斥锁解决缓存击穿问题<br>
 *
 * @author Mike
 * @since 2025/2/25
 */
@Service
public class ConcernServiceImpl extends ServiceImpl<ConcernMapper, Concern> implements IConcernService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;

    /**
     * 根据关注记录ID查询详细信息
     * @param id 关注记录唯一标识
     * @return 包含关注实体或错误信息的Result对象
     */
    @Override
    public Result getConcernById(Integer id) {
        try {
            Concern concern = hashRedisClient.hMultiGet(CACHE_CONCERN_KEY + id, Concern.class);
            if (concern == null) {
                concern = getById(id);
            }
            if (concern == null) {
                return Result.fail("该关注记录ID不存在");
            }
            hashRedisClient.hMultiSet(CACHE_CONCERN_KEY + id, Concern.class);
            hashRedisClient.expire(CACHE_CONCERN_KEY + id, CACHE_CONCERN_TTL, TimeUnit.MINUTES);
            return Result.ok(concern);
        } catch (ParseException e) {
            return Result.fail("获取关注记录ID为" + id + "失败");
        }
    }

    /**
     * 获取指定用户的关注列表
     * @param userId 用户唯一标识
     * @return 包含关注集合的Result对象
     */
    @Override
    public Result getConcernByUserId(Integer userId) {
        List<Concern> concernList = stringRedisClient.queryListWithMutex(
                CACHE_CONCERN_USER_KEY,
                userId,
                Concern.class,
                this::getConcernsFromDBForUserId,
                CACHE_CONCERN_USER_TTL,
                TimeUnit.MINUTES
        );
        if (concernList == null) {
            return Result.fail("获取收藏记录列表失败");
        }
        return Result.ok(concernList);
    }

    /**
     * 统计用户的关注数量
     * @param userId 用户唯一标识
     * @return 包含关注数量的Result对象
     */
    @Override
    public Result getConcernNumByUserId(Integer userId) {
        return Result.ok(baseMapper.selectCount(query().getWrapper().eq("user_id", userId)));
    }

    /**
     * 根据ID删除关注记录
     * @param id 关注记录唯一标识
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeConcernById(Integer id) {
        if (!removeById(id)) {
            throw new RuntimeException("删除关注记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_CONCERN_KEY + id);
        return Result.ok();
    }

    /**
     * 根据用户ID和粉丝ID删除关注关系
     * @param userId 被关注用户ID
     * @param fansId 粉丝用户ID
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result removeConcernByUserIdAndFansId(Integer userId, Integer fansId) {
        Integer id = baseMapper.selectOne(
                query().getWrapper().eq("user_id", userId)
                        .eq("fans_id", fansId)).getId();
        if (!removeById(id)) {
            throw new RuntimeException("删除关注记录" + id + "失败");
        }
        hashRedisClient.delete(CACHE_CONCERN_KEY + id);
        return Result.ok();
    }

    /**
     * 新增关注关系记录
     * @param concern 关注实体对象
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result addConcern(Concern concern) {
        if (!save(concern)) {
            throw new RuntimeException("添加新的关注记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_CONCERN_KEY + concern.getId(), concern);
        return Result.ok();
    }

    /**
     * 从数据库查询用户关注列表（缓存回源方法）
     * @param userId 用户唯一标识
     * @return 关注记录集合（空集合表示无数据）
     */
    private List<Concern> getConcernsFromDBForUserId(Integer userId) {
        List<Concern> concernList = list(new QueryWrapper<Concern>().eq("user_id", userId));
        if (concernList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return concernList;
    }
}
