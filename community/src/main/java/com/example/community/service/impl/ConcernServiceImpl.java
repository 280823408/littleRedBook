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

@Service
public class ConcernServiceImpl extends ServiceImpl<ConcernMapper, Concern> implements IConcernService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
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

    @Override
    public Result getConcernNumByUserId(Integer userId) {
        return Result.ok(baseMapper.selectCount(query().getWrapper().eq("user_id", userId)));
    }

    @Override
    @Transactional
    public Result removeConcernById(Integer id) {
        hashRedisClient.delete(CACHE_CONCERN_KEY + id);
        removeById(id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result removeConcernByUserIdAndFansId(Integer userId, Integer fansId) {
        Integer id = baseMapper.selectOne(
                query().getWrapper().eq("user_id", userId)
                        .eq("fans_id", fansId)).getId();
        hashRedisClient.delete(CACHE_CONCERN_KEY + id);
        removeById(id);
        return Result.ok();
    }

    @Override
    @Transactional
    public Result addConcern(Concern concern) {
        if (!save(concern)) {
            return Result.fail("添加新的关注记录失败");
        }
        hashRedisClient.hMultiSet(CACHE_CONCERN_KEY + concern.getId(), concern);
        return Result.ok();
    }

    private List<Concern> getConcernsFromDBForUserId(Integer userId) {
        List<Concern> concernList = list(new QueryWrapper<Concern>().eq("user_id", userId));
        if (concernList.isEmpty()) {
            return java.util.Collections.emptyList();
        }
        return concernList;
    }
}
