package com.sky.controller.admin;

import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Set;

/**
 * @author: ChuYangjie
 * @date: 2024/3/24 13:13
 * @version: 1.0
 */

/**
 * 菜品管理
 */
@Slf4j
@RestController("adminDishController")
@RequestMapping("/admin/dish")
@Api(tags = "菜品相关接口")
public class DishController {

    @Autowired
    private DishService dishService;

    @Autowired
    private RedisTemplate redisTemplate;

    private static final String PATTERN = "dish_";

    @ApiOperation("新增菜品")
    @PostMapping
    public Result save(@RequestBody DishDTO dishDTO) {
        log.info("新增菜品");
        dishService.saveWithFlavor(dishDTO);

        // 清理缓存
        cleanCache(PATTERN + dishDTO.getCategoryId());
        return Result.success();
    }

    @ApiOperation("菜品分页查询")
    @GetMapping("/page")
    public Result<PageResult> pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        log.info("开始分页查询，分页参数，页码：{}，每页记录数：{}", dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        PageResult pageResult = dishService.pageQuery(dishPageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("批量删除")
    @DeleteMapping
    public Result deleteBatch(@RequestParam List<Long> ids) { // @RequestParam 用来将ids=1,2,3封装进List
        dishService.deleteBatch(ids);

        // 清理所有缓存
        cleanCache(PATTERN);
        return Result.success();
    }

    @ApiOperation("根据id查询菜品")
    @GetMapping("/{id}")
    public Result<DishVO> getById(@PathVariable Long id) {
        log.info("根据id查询菜品");
        DishVO dishVO = dishService.getByIdWithFlavor(id);
        return Result.success(dishVO);
    }

    @ApiOperation("修改菜品")
    @PutMapping
    public Result edit(@RequestBody DishDTO dishDTO) {
        log.info("修改菜品");
        dishService.editWithFlavor(dishDTO);

        // 清理所有缓存
        cleanCache(PATTERN);
        return Result.success();
    }

    @ApiOperation("菜品起售/停售")
    @PostMapping("/status/{status}")
    public Result editStatus(@PathVariable Integer status, Long id) {
        dishService.editStatus(status, id);
        // 清理所有缓存
        cleanCache(PATTERN);
        return Result.success();
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    @GetMapping("/list")
    @ApiOperation("根据分类id查询菜品")
    public Result<List<Dish>> list(Long categoryId){
        List<Dish> list = dishService.list(categoryId);
        return Result.success(list);
    }

    /**
     * 工具方法，用以清理缓存
     * @param pattern
     */
    private void cleanCache(String pattern) {
        Set keys = redisTemplate.keys(pattern + "*");
        redisTemplate.delete(keys);
    }
}
