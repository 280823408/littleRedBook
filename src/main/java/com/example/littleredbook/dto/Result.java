package com.example.littleredbook.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 统一API响应结果封装类
 *
 * <p>功能说明：
 * 1. 标准化接口返回格式<br>
 * 2. 支持成功/失败两种状态封装<br>
 * 3. 支持单对象、列表数据、分页数据等多种返回类型<br>
 * 4. 提供链式静态工厂方法创建实例<br>
 * <br>
 * <p>使用场景：
 * - Controller层统一返回格式<br>
 * - 服务间调用结果包装<br>
 * - 前端接口数据标准化解析<br>
 *
 * @author Mike
 * @since 2025/2/23
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Result {
    /** 操作是否成功 */
    private Boolean success;

    /** 错误提示信息 */
    private String errorMsg;

    /** 返回的业务数据 */
    private Object data;

    /** 分页查询时的总记录数 */
    private Long total;

    /**
     * 创建成功响应（无数据）
     * @return 成功状态的结果对象
     */
    public static Result ok(){
        return new Result(true, null, null, null);
    }

    /**
     * 创建带数据的成功响应
     * @param data 要返回的业务数据
     * @return 包含数据的成功结果对象
     */
    public static Result ok(Object data){
        return new Result(true, null, data, null);
    }

    /**
     * 创建分页查询的成功响应
     * @param data 当前页数据列表
     * @param total 总记录数
     * @return 包含分页数据的结果对象
     */
    public static Result ok(List<?> data, Long total){
        return new Result(true, null, data, total);
    }

    /**
     * 创建失败响应
     * @param errorMsg 错误提示信息
     * @return 失败状态的结果对象
     */
    public static Result fail(String errorMsg){
        return new Result(false, errorMsg, null, null);
    }
}
