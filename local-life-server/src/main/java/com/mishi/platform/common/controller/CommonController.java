package com.mishi.platform.common.controller;

import cn.hutool.core.util.IdUtil;
import com.mishi.platform.common.result.Result;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;

/**
 * 通用接口 — 文件上传
 */
@RestController
@RequestMapping("/api/common")
public class CommonController {

    @Value("${mishi.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        // 文件类型校验
        String originalName = file.getOriginalFilename();
        String ext = "";
        if (originalName != null && originalName.contains(".")) {
            ext = originalName.substring(originalName.lastIndexOf(".")).toLowerCase();
        }
        if (!ext.matches("\\.(jpg|jpeg|png|gif|webp|bmp)$")) {
            throw new com.mishi.platform.common.exception.BusinessException("仅支持图片格式 (jpg/png/gif/webp)");
        }
        if (file.getSize() > 10 * 1024 * 1024) {
            throw new com.mishi.platform.common.exception.BusinessException("文件大小不能超过10MB");
        }
        String fileName = IdUtil.fastSimpleUUID() + ext;

        // 保存到项目根目录下的 uploads/
        File dir = new File(System.getProperty("user.dir"), uploadPath);
        if (!dir.exists()) dir.mkdirs();
        file.transferTo(new File(dir, fileName));

        // 返回访问路径
        String url = "/uploads/" + fileName;
        return Result.success(url);
    }
}
