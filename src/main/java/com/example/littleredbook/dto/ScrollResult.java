package com.example.littleredbook.dto;

import lombok.Data;

import java.util.List;

/**
 * 滚动分页查询结果封装类
 *
 * <p>功能说明：
 * 1. 用于支持基于时间戳的滚动分页查询<br>
 * 2. 适用于社交媒体动态、聊天记录等需要持续加载的场景<br>
 * 3. 提供下次查询的定位参数<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Data
public class ScrollResult {
    /** 当前页数据列表 */
    private List<?> list;

    /** 当前页数据的最小时间戳 */
    private Long minTime;

    /** 数据偏移量 */
    private Integer offset;
}
