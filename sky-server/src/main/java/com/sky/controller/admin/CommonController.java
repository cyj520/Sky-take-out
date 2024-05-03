package com.sky.controller.admin;

/**
 * @author: ChuYangjie
 * @date: 2024/3/24 9:59
 * @version: 1.0
 */

import com.sky.constant.MessageConstant;
import com.sky.result.Result;
import com.sky.utils.AliOssUtil;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.UUID;

/**
 * 通用接口
 */
@Slf4j
@Api(tags = "通用接口")
@RestController
@RequestMapping("/admin/common")
public class CommonController {

    @Autowired
    private AliOssUtil aliOssUtil;

    @ApiOperation("文件上传")
    @PostMapping("/upload")
    public Result<String> upload(MultipartFile file) {
        log.info("文件上传，{}", file);
        try {
            // 生成文件名
            String filename = file.getOriginalFilename();
            String extension = filename.substring(filename.lastIndexOf('.'));
            String newFileName = UUID.randomUUID().toString() + extension;
            String uploadUrl = aliOssUtil.upload(file.getBytes(), newFileName);
            return Result.success(uploadUrl);
        } catch (IOException e) {
            log.error("上传失败，{}", e);
        }
        return Result.error(MessageConstant.UPLOAD_FAILED);
    }
}
