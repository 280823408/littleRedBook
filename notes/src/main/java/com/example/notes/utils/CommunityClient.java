package com.example.notes.utils;

import com.example.littleredbook.config.FeignConfiguration;
import com.example.littleredbook.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/**
 * 标签服务Feign客户端
 *
 * <p>功能说明：
 * 1. 声明式标签服务HTTP调用接口<br>
 * 2. 对接社区服务标签模块核心接口<br>
 * 3. 支持服务发现与直连两种调用模式<br>
 * 4. 实现笔记-标签关联关系查询<br>
 *
 * <p>配置说明：
 * - 服务名称：community（对应注册中心服务ID）<br>
 * - 默认直连地址：http://localhost:8100<br>
 * - 接口基础路径：/tag<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
@FeignClient(name = "community", url = "http://localhost:8100", configuration = FeignConfiguration.class)
public interface CommunityClient {
    /**
     * 查询笔记关联标签
     * @param noteId 笔记唯一标识
     * @return Result标准响应（包含List<Tag>或错误信息）
     */
    @GetMapping("/tags/notes/{noteId}")
    Result getTagsByNoteId(@PathVariable Integer noteId);

    /**
     * 查询标签关联笔记
     * @param tagId 标签唯一标识
     * @return Result标准响应（包含List<Integer>笔记ID集合）
     */
    @GetMapping("/tags/{tagId}/notes")
    Result getNoteIdByTagId(@PathVariable Integer tagId);

    @GetMapping("/concern/user/{userId}/friends")
    Result getFriends(@PathVariable Integer userId);
}
