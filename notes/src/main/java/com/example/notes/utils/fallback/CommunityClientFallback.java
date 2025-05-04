package com.example.notes.utils.fallback;

import com.example.littleredbook.dto.Result;
import com.example.notes.utils.CommunityClient;

/**
 * 标签服务熔断降级处理类
 *
 * <p>功能说明：
 * 1. 实现Feign客户端的服务降级逻辑<br>
 * 2. 保障标签关联查询的弹性容错能力<br>
 * 3. 提供标签服务不可用时的统一降级响应<br>
 * 4. 防止服务雪崩效应扩散至笔记核心业务<br>
 *
 * <p>触发场景：
 * - 标签服务响应超时（默认1秒）<br>
 * - 标签服务实例不可用<br>
 * - 服务请求量超过熔断阈值<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
public class CommunityClientFallback implements CommunityClient {
    /**
     * 笔记标签查询降级处理
     * @param noteId 笔记唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getTagsByNoteId(Integer noteId) {
        return Result.fail("标签服务不可用");
    }

    /**
     * 标签笔记关联查询降级处理
     * @param tagId 标签唯一标识
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getNoteIdByTagId(Integer tagId) {
        return Result.fail("标签服务不可用");
    }

    @Override
    public Result getFriends(Integer userId) {
        return Result.fail("标签服务不可用");
    }
}
