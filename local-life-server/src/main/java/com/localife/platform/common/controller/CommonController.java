package com.localife.platform.common.controller;

import cn.hutool.core.util.IdUtil;
import com.localife.platform.common.result.Result;
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

    @Value("${local-life.upload.path:uploads}")
    private String uploadPath;

    @PostMapping("/upload")
    public Result<String> upload(@RequestParam("file") MultipartFile file) throws IOException {
        String originalName = file.getOriginalFilename();
        String ext = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf(".")) : ".png";
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
