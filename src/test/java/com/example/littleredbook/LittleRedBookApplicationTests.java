package com.example.littleredbook;

import com.example.littleredbook.entity.User;
import com.example.littleredbook.utils.HashRedisClient;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest
class LittleRedBookApplicationTests {
    @Resource
    private HashRedisClient hashRedisClient;
    @Test
    void contextLoads() {
        User user = new User();
        user.setId(1);
        user.setUserName("test");
        user.setUserPassword("123456");
        user.setPhone("12345678901");
        user.setIcon("test");
        user.setInfo("test");
        hashRedisClient.hMultiSet("test",user);
    }

}
