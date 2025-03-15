package com.example.messages.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.example.littleredbook.entity.LikeComment;
/**
 * 点赞评论数据访问层接口
 *
 * <p>功能说明：
 * 1. 继承MyBatis-Plus基础Mapper实现点赞评论数据CRUD操作<br>
 * 2. 提供默认的点赞评论表基础数据库操作方法<br>
 * 3. 支持配合MyBatis-Plus条件构造器进行复杂查询<br>
 * 4. 支持关联查询（用户信息、评论内容等）<br>
 *
 * <p>使用规范：
 * - 需在启动类配置{@code @MapperScan}注解扫描<br>
 * - 复杂关联查询建议通过XML文件实现<br>
 * - 分页查询需配合MyBatis-Plus分页插件使用<br>
 *
 * @author Mike
 * @since 2025/3/9
 */
public interface LikeCommentMapper extends BaseMapper<LikeComment> {
}
