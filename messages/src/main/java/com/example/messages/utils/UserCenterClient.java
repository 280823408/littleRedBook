package com.example.messages.utils;

import com.example.littleredbook.config.FeignConfiguration;
import com.example.littleredbook.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

/**
 * 用户服务Feign客户端
 *
 * <p>功能说明：
 * 1. 声明式用户服务HTTP调用接口<br>
 * 2. 对接用户模块核心接口<br>
 * 3. 支持单体/微服务架构切换（通过URL配置）<br>
 * 4. 实现服务间用户数据交互<br>
 *
 * <p>配置说明：
 * - 服务名称：login（对应注册中心服务ID）<br>
 * - 默认直连地址：http://localhost:8101<br>
 * - 接口基础路径：/user<br>
 *
 * @author Mike
 * @since 2025/2/24
 */
@FeignClient(name = "userCenter", url = "http://localhost:8101", configuration = FeignConfiguration.class)
public interface UserCenterClient {
    /**
     * 根据用户ID查询用户信息
     * @param id 用户唯一标识
     * @return Result标准响应（包含UserDTO或错误信息）
     */
    @GetMapping("/users/{id}")
    Result getUserById(@PathVariable Integer id);
}
