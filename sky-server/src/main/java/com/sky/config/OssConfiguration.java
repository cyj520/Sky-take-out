package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author: ChuYangjie
 * @date: 2024/3/24 10:24
 * @version: 1.0
 */

/**
 * 配置类，用于初始化OSSUtils
 */
@Slf4j
@Configuration
public class OssConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil(AliOssProperties properties) {
        log.info("开始创建AliOSSUtil对象,{}", properties);
        return new AliOssUtil(properties.getEndpoint(),
                properties.getAccessKeyId(),
                properties.getAccessKeySecret(),
                properties.getBucketName());
    }
}
