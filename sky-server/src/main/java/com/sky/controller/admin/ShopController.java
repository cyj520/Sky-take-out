package com.sky.controller.admin;

import com.sky.result.Result;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import static com.sky.constant.ShopStatusConstant.KEY;

/**
 * @author: ChuYangjie
 * @date: 2024/3/25 13:28
 * @version: 1.0
 */

// 区分Controller
@RestController("adminShopController")
@RequestMapping("/admin/shop")
@Api(tags = "店铺相关接口")
@Slf4j
public class ShopController {
    @Autowired
    private RedisTemplate redisTemplate;

//    @Autowired
//    private ;
    @ApiOperation("设置店铺状态")
    @PutMapping("/{status}")
    public Result setStatus(@PathVariable Integer status) {
        log.info("设置营业状态为{}", status == 1 ? "营业" : "打烊");
        redisTemplate.opsForValue().set(KEY, status);
        return Result.success();
    }

    @ApiOperation("获取店铺状态")
    @GetMapping("/status")
    public Result<Integer> getStatus() {
        Integer status = (Integer) redisTemplate.opsForValue().get(KEY);
        log.info("获取当前营业状态为{}", status);
        return Result.success(status);
    }
}
