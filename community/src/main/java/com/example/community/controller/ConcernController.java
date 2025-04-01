package com.example.community.controller;

import com.example.community.service.IConcernService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Concern;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

/**
 * 用户关注关系控制器
 *
 * <p>功能说明：
 * 1. 处理用户关注模块相关HTTP请求入口<br>
 * 2. 提供关注关系的增删查等RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回Result标准响应格式<br>
 * 5. 包含以下核心接口：<br>
 *   - 关注记录单点查询<br>
 *   - 用户关注列表获取<br>
 *   - 关注数量统计<br>
 *   - 关注关系移除操作<br>
 *   - 新增关注关系创建<br>
 *
 * @author Mike
 * @since 2025/3/6
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("concern")
public class ConcernController {
    @Resource
    private IConcernService concernService;

    /**
     * 获取指定ID的关注记录
     * @param id 关注记录唯一标识
     * @return 包含关注实体或错误信息的Result对象
     */
    @GetMapping("/{id}")
    public Result getConcernById(@PathVariable Integer id) {
        return concernService.getConcernById(id);
    }

    /**
     * 获取指定用户的关注列表
     * @param userId 用户唯一标识
     * @return 包含关注关系集合的Result对象
     */
    @GetMapping("/user/{userId}")
    public Result getConcernByUserId(@PathVariable Integer userId) {
        return concernService.getConcernByUserId(userId);
    }

    /**
     * 统计用户的关注数量
     * @param userId 用户唯一标识
     * @return 包含关注数量的Result对象
     */
    @GetMapping("/user/{userId}/count")
    public Result getConcernNumByUserId(@PathVariable Integer userId) {
        return concernService.getConcernNumByUserId(userId);
    }

    /**
     * 获取指定用户与粉丝的关注关系
     * @param userId 用户唯一标识
     * @param fansId 粉丝用户唯一标识
     * @return 包含关注关系实体的Result对象
     */
    @GetMapping("/user/{userId}/fans/{fansId}")
    public Result getConcernByUserIdAndFansId(
            @PathVariable Integer userId,
            @PathVariable Integer fansId) {
        return concernService.getConcernByUserIdAndFansId(userId, fansId);
    }

    /**
     * 获取用户关注通知
     * @param userId 用户唯一标识
     * @return 包含关注通知的Result对象
     */
    @GetMapping("/notices/{userId}")
    public Result getConcernNotice(@PathVariable Integer userId) {
        return concernService.getConcernNotice(userId);
    }

    /**
     * 通过ID移除关注关系
     * @param id 关注记录唯一标识
     * @return 操作结果的Result对象
     */
    @DeleteMapping("/{id}")
    public Result removeConcernById(@PathVariable Integer id) {
        return concernService.removeConcernById(id);
    }

    /**
     * 通过用户与粉丝ID解除关注关系
     * @param userId 被关注用户ID
     * @param fansId 粉丝用户ID
     * @return 操作结果的Result对象
     */
    @DeleteMapping("/user/{userId}/fans/{fansId}")
    public Result removeConcernByUserIdAndFansId(
            @PathVariable Integer userId,
            @PathVariable Integer fansId) {
        return concernService.removeConcernByUserIdAndFansId(userId, fansId);
    }

    /**
     * 新增用户关注关系
     * @param concern 待新增的关注关系数据传输对象
     * @return 操作结果的Result对象
     */
    @PostMapping
    public Result addConcern(@RequestBody Concern concern) {
        return concernService.addConcern(concern);
    }
}
