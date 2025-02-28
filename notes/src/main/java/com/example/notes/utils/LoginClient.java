package com.example.notes.utils;

import com.example.littleredbook.dto.Result;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@FeignClient(name = "login",url = "http://localhost:8101", path = "/user")
public interface LoginClient {
    @PostMapping("getUserById")
    Result getUserById(@RequestParam Integer id);
    @PostMapping("getUsersByIds")
    Result getUsersByIds(@RequestParam List<Integer> ids);
}
