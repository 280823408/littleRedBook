package com.example.notes.utils;

import com.example.littleredbook.dto.Result;

import java.util.List;

/**
 * 用户服务熔断降级处理类
 *
 * <p>功能说明：
 * 1. 实现Feign客户端的服务降级逻辑<br>
 * 2. 保障核心业务流程的弹性容错能力<br>
 * 3. 提供用户服务不可用时的友好提示<br>
 * 4. 防止服务雪崩效应发生<br>
 *
 * <p>触发场景：
 * - 用户服务响应超时（默认1秒）<br>
 * - 用户服务实例不可用<br>
 * - 服务请求量超过熔断阈值<br>
 *
 * @author Mike
 * @since 2025/2/24
 */
public class UserCenterClientFallback implements UserCenterClient {
    /**
     * 用户查询降级处理
     * @param id 用户ID
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getUserById(Integer id) {
        return Result.fail("用户服务不可用");
    }

    /**
     * 批量查询降级处理
     * @param ids 用户ID列表
     * @return 固定错误响应（服务不可用提示）
     */
    @Override
    public Result getUsersByIds(List<Integer> ids) {
        return Result.fail("用户服务不可用");
    }
}
