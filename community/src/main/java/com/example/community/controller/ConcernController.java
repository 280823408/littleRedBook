package com.example.community.controller;

import com.example.community.service.IConcernService;
import com.example.littleredbook.dto.Result;
import com.example.littleredbook.entity.Concern;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("concern")
public class ConcernController {
    @Resource
    private IConcernService concernService;
    @GetMapping("getConcernById")
    public Result getConcernById(@RequestParam Integer id) {
        return concernService.getConcernById(id);
    }

    @GetMapping("getConcernByUserId")
    public Result getConcernByUserId(@RequestParam Integer userId) {
        return concernService.getConcernByUserId(userId);
    }

    @GetMapping("getConcernNumByUserId")
    public Result getConcernNumByUserId(@RequestParam Integer userId) {
        return concernService.getConcernNumByUserId(userId);
    }

    @GetMapping("removeConcernById")
    public Result removeConcernById(@RequestParam Integer id) {
        return concernService.removeConcernById(id);
    }

    @GetMapping("removeConcernByUserIdAndFansId")
    public Result removeConcernByUserIdAndFansId(@RequestParam Integer userId, @RequestParam Integer fansId) {
        return concernService.removeConcernByUserIdAndFansId(userId, fansId);
    }

    @PostMapping("addConcern")
    public Result addConcern(@RequestBody Concern concern) {
        return concernService.addConcern(concern);
    }
}
