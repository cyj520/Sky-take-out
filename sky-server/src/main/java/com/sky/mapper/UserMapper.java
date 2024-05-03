package com.sky.mapper;

import com.sky.entity.User;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import java.util.Map;

@Mapper
public interface UserMapper {


    /**
     * 根据Openid查询用户
     * @param openid
     */
    @Select("select id, openid, name, phone, sex, id_number, avatar, create_time " +
            "from user where openid = #{openid}")
    User selectByOpenid(String openid);

    /**
     * 插入新用户
     * @param user
     */
    void insert(User user);

    /**
     * 根据主键id查询用户
     * @param id
     * @return
     */
    @Select("select id, openid, name, phone, sex, id_number, avatar, create_time " +
            "from user where id = #{id}")
    User getById(Long id);

    /**
     * 根据动态条件查询用户数量
     * @param map
     * @return
     */
    Integer countByMap(Map map);
}
