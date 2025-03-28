package com.yxc.item.controller;

import com.yxc.common.domain.Result;
import com.yxc.item.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import javax.annotation.Resource;
import java.io.IOException;
import java.util.Objects;
import java.util.UUID;

@RestController
@RequestMapping("/upload")
@Slf4j
public class UploadController {

    @Resource
    AliOssUtil aliOssUtil;

    @PostMapping
    public Result upload(@RequestBody MultipartFile file) {
        try {
            String[] split = Objects.requireNonNull(file.getOriginalFilename()).split("\\.");
            if(split.length == 0){
                return Result.error("文件格式错误");
            }
            String filename = UUID.randomUUID().toString() + "." + split[split.length - 1];
            String result = aliOssUtil.upload(file.getBytes(), filename);
            return Result.ok(result);
        } catch (IOException e) {
            log.info("文件上传失败：{}", e);
        }
        return Result.error("文件上传失败");
    }
}
