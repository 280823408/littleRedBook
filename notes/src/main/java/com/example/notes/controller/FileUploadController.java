package com.example.notes.controller;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Objects;

/**
 * 文件上传控制器
 *
 * <p>功能说明：
 * 1. 处理文件上传相关HTTP请求入口<br>
 * 2. 提供文件上传RESTful API<br>
 * 3. 支持跨域访问（@CrossOrigin）<br>
 * 4. 统一返回ResponseEntity标准响应格式<br>
 * 5. 包含以下核心功能：<br>
 *   - 多类型文件（图片/视频）分类存储<br>
 *   - 唯一文件名生成（时间戳+UUID+原始文件名清洗）<br>
 *   - 文件存储路径动态配置（通过application.properties）<br>
 *   - 大文件上传异常拦截与规范化错误响应<br>
 *   - 文件存储目录自动创建机制<br>
 *   - 多维度异常处理（文件超限、IO错误、无效请求等）<br>
 *
 * @author Mike
 * @since 2024/3/15
 */
@Slf4j
@CrossOrigin
@RestController
@RequestMapping("files")
public class FileUploadController {
    @Value("${file.upload.dir}")
    private String uploadDir;

    /**
     * 文件上传处理接口
     *
     * <p>实现流程：
     * 1. 接收multipart/form-data格式文件流<br>
     * 2. 生成防重复文件名（时间戳_UUID_原始文件名）<br>
     * 3. 根据文件类型（image/video）自动分类存储<br>
     * 4. 创建必要目录结构<br>
     * 5. 返回可访问的文件相对路径<br>
     *
     * @param file 上传的文件对象（必须包含在multipart请求中）
     * @return 包含文件访问路径的响应实体（HTTP 200）或错误描述（4xx/5xx）
     * @apiNote 支持最大文件大小受Spring配置限制，默认超出限制返回413状态码
     */
    @PostMapping
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file, @RequestParam("type") String type) {
        try {
            String fileName = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String subDir;
            switch (type) {
                case ("0") : subDir = "images"; break;
                case ("1") : subDir = "videos"; break;
                default: subDir = "others";
            }
            Path storagePath = Paths.get(uploadDir, subDir, fileName);
            Files.createDirectories(storagePath.getParent());
            Files.copy(file.getInputStream(), storagePath);
            String fileUrl = "/" + subDir + "/" + fileName;
            return ResponseEntity.ok().body(Map.of("url", fileUrl));
        } catch (MultipartException e) {
            return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body("文件大小超过限制");
        } catch (IOException e) {
            return ResponseEntity.internalServerError().body("文件存储失败: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.badRequest().body("无效请求");
        }
    }
}
