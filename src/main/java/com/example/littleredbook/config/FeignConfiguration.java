package com.example.littleredbook.config;

import feign.Feign;
import feign.Request;
import feign.Retryer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Feign客户端全局配置类
 *
 * <p>功能说明：
 * 1. 配置Feign客户端的HTTP请求参数<br>
 * 2. 定义全局超时控制策略<br>
 * 3. 设置服务间调用的重试机制<br>
 * 4. 统一微服务通信的基础设施配置<br>
 *
 * <p>核心配置项：
 * - 连接超时时间（默认50000ms）<br>
 * - 读取超时时间（默认50000ms）<br>
 * - 重试策略配置（间隔/最大间隔/次数）<br>
 *
 * <p>最佳实践：
 * - 超时时间应根据下游服务SLA设定<br>
 * - 重试次数需结合业务幂等性考虑<br>
 * - 生产环境建议启用断路器模式<br>
 *
 * @author Mike
 * @since 2025/3/15
 */
@Configuration
public class FeignConfiguration {

    /**
     * 配置Feign请求超时参数
     *
     * <p>参数说明：
     * 1. connectTimeoutMillis：建立TCP连接等待时间（默认50000ms）<br>
     * 2. readTimeoutMillis：等待响应数据的超时时间（默认50000ms）<br>
     *
     * <p>配置建议：
     * - 短连接服务可适当缩短超时时间<br>
     * - 批量处理服务需延长读取超时<br>
     * - 结合Hystrix熔断超时综合设置<br>
     *
     * @return 全局请求配置对象
     */
    @Bean
    public Request.Options feignOptions() {
        return new Request.Options(50000, 50000);
    }

    /**
     * 构建Feign客户端全局构造器
     *
     * <p>功能特性：
     * 1. 集成预定义的超时配置<br>
     * 2. 配置默认重试策略（间隔100ms，最大间隔1s，重试3次）<br>
     * 3. 支持自定义扩展编解码器等组件<br>
     *
     * @param options 请求超时配置
     * @return 预配置的Feign构造器
     */
    @Bean
    public Feign.Builder feignBuilder(Request.Options options) {
        return Feign.builder()
                .options(options)
                .retryer(new Retryer.Default(100, 1000, 3));
    }
}
