package com.sky.mapper;

import com.sky.entity.DishFlavor;
import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Mapper;

import java.util.List;

@Mapper
public interface DishFlavorMapper {
    /**
     * 批量插入
     * @param flavors
     */
    void insertBatch(List<DishFlavor> flavors);

    /**
     * 根据菜品id删除对应口味
     * @param dishId
     */
    @Delete("delete from dish_flavor where id = #{id}")
    void deleteBatchByDishId(Long dishId);
    /**
     * 根据菜品id批量删除对应口味
     * @param ids
     */
    void deleteBatchByDishIds(List<Long> ids);

    /**
     * 根据菜品id查询对应口味
     * @param dishId
     * @return
     */
    List<DishFlavor> getByDishId(Long dishId);
}
