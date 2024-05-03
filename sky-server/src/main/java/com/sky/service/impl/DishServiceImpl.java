package com.sky.service.impl;

import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.sky.constant.MessageConstant;
import com.sky.constant.StatusConstant;
import com.sky.dto.DishDTO;
import com.sky.dto.DishPageQueryDTO;
import com.sky.entity.Dish;
import com.sky.entity.DishFlavor;
import com.sky.entity.Setmeal;
import com.sky.exception.DeletionNotAllowedException;
import com.sky.mapper.DishFlavorMapper;
import com.sky.mapper.DishMapper;
import com.sky.mapper.SetmealDishMapper;
import com.sky.mapper.SetmealMapper;
import com.sky.result.PageResult;
import com.sky.service.DishService;
import com.sky.vo.DishVO;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * @author: ChuYangjie
 * @date: 2024/3/24 13:19
 * @version: 1.0
 */
@Service
public class DishServiceImpl implements DishService {

    @Autowired
    private SetmealMapper setmealMapper;
    @Autowired
    private SetmealDishMapper setmealDishMapper;
    @Autowired
    private DishMapper dishMapper;
    @Autowired
    private DishFlavorMapper dishFlavorMapper;
    /**
     * 新增菜品和口味
     * @param dishDTO
     */
    @Transactional // 开启事务，保证数据一致性
    @Override
    public void saveWithFlavor(DishDTO dishDTO) {
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        // 菜品表插入数据
        dishMapper.insert(dish);
        Long dishId = dish.getId();// 需要在mapper.xml文件中配置useGeneratedKeys和keyProperty，才能获取主键的自增值
        // 口味表插入数据
        List<DishFlavor> flavors = dishDTO.getFlavors();
        if (flavors != null && flavors.size() != 0) {
            flavors.forEach(dishFlavor -> dishFlavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavors);
        }
    }

    /**
     * 菜品分页查询
     * @param dishPageQueryDTO
     * @return
     */
    @Override
    public PageResult pageQuery(DishPageQueryDTO dishPageQueryDTO) {
        PageHelper.startPage(dishPageQueryDTO.getPage(), dishPageQueryDTO.getPageSize());
        Page<DishVO> dishVOPage = dishMapper.pageQuery(dishPageQueryDTO);
        return new PageResult(dishVOPage.getTotal(), dishVOPage.getResult());
    }

    /**
     * 批量删除菜品
     * 1.菜品在售不能删status=1
     * 2.菜品有关联的套餐不能删
     * 3.删除菜品后，删除对应的口味
     * @param ids
     */
    @Transactional
    @Override
    public void deleteBatch(List<Long> ids) {
        // 检验在售状态
        for (Long id : ids) {
            Dish dish = dishMapper.getById(id);
            if (dish.getStatus() == StatusConstant.ENABLE) {
                throw new DeletionNotAllowedException(MessageConstant.DISH_ON_SALE);
            }
        }

        // 检验与菜品相关联的套餐
        List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(ids);
        if (setmealIds != null && setmealIds.size() > 0) {
            throw new DeletionNotAllowedException(MessageConstant.DISH_BE_RELATED_BY_SETMEAL);
        }

        // 删除菜品
        dishMapper.deleteBatch(ids);
        // 删除对应口味
        dishFlavorMapper.deleteBatchByDishIds(ids);
    }

    /**
     * 修改菜品信息
     * @param dishDTO
     */
    @Transactional
    @Override
    public void editWithFlavor(DishDTO dishDTO) {
        Long dishId = dishDTO.getId();
        // 选择查询得到dish而非新建，因为新建的dish不包含create_time等信息
        // Dish dish = dishMapper.getById(dishId);
        // 更正：update的参数不需要包含create_time，是null则不会更新数据库
        Dish dish = new Dish();
        BeanUtils.copyProperties(dishDTO, dish);
        dishMapper.update(dish);
        // 获取口味信息
        List<DishFlavor> flavorList =  dishDTO.getFlavors();
        // 修改口味表，先删再增
        dishFlavorMapper.deleteBatchByDishId(dishId);
        if (flavorList != null && flavorList.size() != 0) {
            flavorList.forEach(flavor -> flavor.setDishId(dishId));
            dishFlavorMapper.insertBatch(flavorList);
        }
    }

    /**
     * 根据id查询菜品信息(基本信息+口味信息)
     * @param id
     * @return
     */
    @Override
    public DishVO getByIdWithFlavor(Long id) {
        DishVO dishVO = new DishVO();
        // 查询出菜品基本信息
        Dish dish = dishMapper.getById(id);
        BeanUtils.copyProperties(dish, dishVO);

        // 查询出对应的口味信息
        List<DishFlavor> flavorList = dishFlavorMapper.getByDishId(id);
        dishVO.setFlavors(flavorList);
        return dishVO;
    }

    /**
     * 菜品起售/停售
     * @param status
     * @param id
     */
    @Override
    public void editStatus(Integer status, Long id) {
        dishMapper.update(Dish.builder()
                .status(status)
                .id(id)
                .build());

        if (status == StatusConstant.DISABLE) {
            // 如果是停售操作，还需要将包含当前菜品的套餐也停售
            List<Long> dishIds = new ArrayList<>();
            dishIds.add(id);
            // select setmeal_id from setmeal_dish where dish_id in (?,?,?)
            List<Long> setmealIds = setmealDishMapper.getSetmealIdsByDishIds(dishIds);
            if (setmealIds != null && setmealIds.size() > 0) {
                for (Long setmealId : setmealIds) {
                    Setmeal setmeal = Setmeal.builder()
                            .id(setmealId)
                            .status(StatusConstant.DISABLE)
                            .build();
                    setmealMapper.update(setmeal);
                }
            }
        }
    }

    /**
     * 根据分类id查询菜品
     * @param categoryId
     * @return
     */
    public List<Dish> list(Long categoryId) {
        Dish dish = Dish.builder()
                .categoryId(categoryId)
                .status(StatusConstant.ENABLE)
                .build();
        return dishMapper.list(dish);
    }

    /**
     * 条件查询菜品和口味
     * @param dish
     * @return
     */
    public List<DishVO> listWithFlavor(Dish dish) {
        List<Dish> dishList = dishMapper.list(dish);

        List<DishVO> dishVOList = new ArrayList<>();

        for (Dish d : dishList) {
            DishVO dishVO = new DishVO();
            BeanUtils.copyProperties(d,dishVO);

            //根据菜品id查询对应的口味
            List<DishFlavor> flavors = dishFlavorMapper.getByDishId(d.getId());

            dishVO.setFlavors(flavors);
            dishVOList.add(dishVO);
        }

        return dishVOList;
    }
}
