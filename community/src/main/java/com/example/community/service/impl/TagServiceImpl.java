package com.example.community.service.impl;

import cn.hutool.core.util.StrUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.example.community.mapper.TagMapper;
import com.example.community.service.ITagService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Tag;
import com.example.littleredbook.utils.HashRedisClient;
import com.example.littleredbook.utils.StringRedisClient;
import jakarta.annotation.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.concurrent.TimeUnit;

import static com.example.littleredbook.utils.RedisConstants.*;

/**
 * 标签服务实现类
 *
 * <p>功能说明：
 * 1. 实现标签领域核心业务逻辑<br>
 * 2. 整合MyBatis-Plus完成数据持久化操作<br>
 * 3. 使用Redis实现标签列表缓存机制<br>
 * 4. 包含标签查询、新增等基础服务<br>
 * 5. 事务注解控制数据库操作原子性<br>
 *
 * <p>关键方法：
 * - 标签查询（ID/全量）<br>
 * - 标签新增及缓存更新<br>
 * - 笔记关联标签待实现<br>
 *
 * @author Mike
 * @since 2025/2/25
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements ITagService {
    @Resource
    private StringRedisClient stringRedisClient;
    @Resource
    private HashRedisClient hashRedisClient;
    /**
     * 根据标签ID查询标签信息
     * @param id 标签唯一标识
     * @return 包含标签实体或错误信息的Result对象
     */
    @Override
    public Result getTagById(Integer id) {
        Tag tag = getById(id);
        if (tag == null) {
            return Result.fail("标签不存在");
        }
        return Result.ok(tag);
    }

    /**
     * 获取系统中所有标签列表
     * @return 包含标签集合的Result对象
     */
    @Override
    public Result getAllTags() {
        String tagJson = stringRedisClient.get(CACHE_TAGLIST_KEY);
        if (!StrUtil.isBlank(tagJson)) {
            return Result.ok(JSONUtil.toList(tagJson, Tag.class));
        }
        List<Tag> tags = list();
        if (tags.isEmpty()) {
            return Result.fail("标签不存在");
        }
        stringRedisClient.set(CACHE_TAGLIST_KEY, JSONUtil.toJsonStr(tags), CACHE_TAGLIST_TTL, TimeUnit.MINUTES);
        return Result.ok(tags);
    }

    /**
     * 新增标签实体记录
     * @param tag 待新增的标签对象
     * @return 操作结果的Result对象
     */
    @Override
    @Transactional
    public Result addTag(Tag tag) {
        if (!save(tag)) {
            throw new RuntimeException("添加新标签失败");
        }
        stringRedisClient.delete(CACHE_TAG_KEY + tag.getId());
        return Result.ok();
    }

    /**
     * 获取指定笔记关联的所有标签
     * @param noteId 笔记唯一标识
     * @return 待实现的Result对象
     */
    @Override
    public Result getTagsByNoteId(Integer noteId) {
        return Result.ok(baseMapper.selectTagsByNoteId(noteId));
    }

    @Override
    public Result getNoteIdByTagId(Integer tagId) {
        return Result.ok(baseMapper.selectNoteIdByTagId(tagId));
    }

    /**
     * 插入标签与笔记的关联关系（待实现）
     * @param tagId 标签ID
     * @param noteId 笔记ID
     * @return 待实现的Result对象
     */
    @Override
    @Transactional
    public Result addNoteTag(Integer tagId, Integer noteId) {
        return Result.ok(baseMapper.insertNoteTag(tagId, noteId));
    }
}
