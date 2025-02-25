package com.example.littleredbook.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus 核心配置类
 *
 * <p>功能说明：
 * 1. 配置MyBatis-Plus扩展功能<br>
 * 2. 注册插件拦截器<br>
 * 3. 支持分页查询功能<br>
 *<br>
 * <p>主要配置项：
 * - 分页插件（PaginationInnerInterceptor）<br>
 * - 自定义SQL拦截器（可按需添加）<br>
 *<br>
 * <p>使用要求：
 * - 需配合@MapperScan注解使用<br>
 * - 依赖mybatis-plus-boot-starter<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Configuration
public class MybatisConfig {

    /**
     * 配置MyBatis-Plus拦截器链
     *
     * <p>核心功能：
     * 1. 添加分页插件，自动识别分页参数<br>
     * 2. 生成适配MySQL数据库的分页SQL<br>
     * 3. 防止全表更新与删除<br>
     *
     * <p>配置说明：
     * - 指定数据库类型为MySQL<br>
     * - 分页参数合理化（pageNum<=0时自动重置为1）<br>
     * - 支持Overflow模式（pageNum超限时返回最后一页）<br>
     *
     * @return 配置完成的拦截器实例
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        PaginationInnerInterceptor pageInterceptor = new PaginationInnerInterceptor(DbType.MYSQL);
        pageInterceptor.setMaxLimit(1000L);
        interceptor.addInnerInterceptor(pageInterceptor);
        return interceptor;
    }
}
