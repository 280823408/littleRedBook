package com.example.notes.utils;

import com.example.littleredbook.dto.Result;

import java.util.List;

public class LoginClientFallback implements LoginClient {
    @Override
    public Result getUserById(Integer id) {
        return Result.fail( "用户服务不可用");
    }

    @Override
    public Result getUsersByIds(List<Integer> ids) {
        return Result.fail( "用户服务不可用");
    }
}
