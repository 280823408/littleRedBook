package com.example.littleredbook.config;

import com.example.littleredbook.dto.Result;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class WebExceptionAdvice {
    /**
     * 全局异常处理：捕获运行时异常（RuntimeException）。
     * 当应用程序抛出 RuntimeException 时，此方法会记录错误日志并返回统一的失败响应。
     *
     * @param e 捕获的运行时异常
     * @return Result 返回统一的失败响应，提示“服务器异常”
     */
    @ExceptionHandler(RuntimeException.class)
    public Result handleRuntimeException(RuntimeException e) {
        log.error(e.toString(), e);
        return Result.fail("服务器异常");
    }
}
