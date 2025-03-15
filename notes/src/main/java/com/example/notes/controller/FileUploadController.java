package com.example.notes.controller;

import cn.hutool.core.lang.UUID;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
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

@Slf4j
@CrossOrigin
@Controller
@RequestMapping("fileUpload")

public class FileUploadController {
    @Value("${file.upload-dir}")
    private String uploadDir;

    @PostMapping("upload")
    @ResponseBody
    public ResponseEntity<?> uploadFile(@RequestParam("file") MultipartFile file) {
        try {
            String fileName = System.currentTimeMillis() + "_"
                    + UUID.randomUUID() + "_"
                    + StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
            String fileType = Objects.requireNonNull(file.getContentType()).split("/")[0];
            String subDir = fileType.equals("image") ? "images" : "videos";
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
