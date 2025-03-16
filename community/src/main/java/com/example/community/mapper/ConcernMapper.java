package com.example.community.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.littleredbook.entity.Concern;

/**
 * 用户关注关系数据访问层接口
 *
 * <p>功能说明：
 * 1. 继承MyBatis-Plus基础Mapper实现关注关系数据CRUD操作<br>
 * 2. 提供默认的关注关系表基础数据库操作方法<br>
 * 3. 支持配合MyBatis-Plus条件构造器进行复杂查询（如双向关注关系查询）<br>
 *
 * <p>使用规范：
 * - 需在启动类配置{@code @MapperScan}注解扫描
 * - 自定义SQL需通过XML文件或注解方式实现
 *
 * <p>业务场景：
 * - 用户关注/取消关注操作
 * - 粉丝关系双向查询
 * - 关注数量统计分析
 *
 * @author Mike
 * @since 2025/3/6
 */
public interface ConcernMapper extends BaseMapper<Concern> {
}
