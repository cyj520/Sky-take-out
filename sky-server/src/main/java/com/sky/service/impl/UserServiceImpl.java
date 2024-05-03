package com.sky.service.impl;

import com.alibaba.fastjson.JSONObject;
import com.sky.constant.MessageConstant;
import com.sky.constant.WxLoginConstant;
import com.sky.dto.UserLoginDTO;
import com.sky.entity.User;
import com.sky.exception.LoginFailedException;
import com.sky.mapper.UserMapper;
import com.sky.properties.WeChatProperties;
import com.sky.service.UserService;
import com.sky.utils.HttpClientUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * @author: ChuYangjie
 * @date: 2024/3/25 16:58
 * @version: 1.0
 */
@Service
@Slf4j
public class UserServiceImpl implements UserService {

    @Autowired
    private WeChatProperties weChatProperties;

    @Autowired
    private UserMapper userMapper;
    /**
     * 微信登录
     * @param userLoginDTO
     * @return
     */
    @Override
    public User wxLogin(UserLoginDTO userLoginDTO) {
        String openid = getOpenid(userLoginDTO.getCode());

        // 判断openid是否存在
        if (openid == null) {
            throw new LoginFailedException(MessageConstant.LOGIN_FAILED);
        }

        // 判断是否为新用户
        User user = userMapper.selectByOpenid(openid);
        if (user == null) {
            user = User.builder()
                    .openid(openid)
                    .createTime(LocalDateTime.now())
                    .build();
            userMapper.insert(user);
        }

        return user;
    }

    /**
     * 工具方法，用于调用微信接口，获取当前用户openid
     * @param code
     * @return
     */
    private String getOpenid(String code) {
        // 构建get请求参数
        Map<String, String> map = new HashMap<>();
        map.put(WxLoginConstant.APPID, weChatProperties.getAppid()); // param1调用常量字符串"appid"，避免硬编码，param2调用自动注入的配置类，从配置文件中获取appid
        map.put(WxLoginConstant.SECRET, weChatProperties.getSecret());
        map.put(WxLoginConstant.JS_CODE, code);
        map.put(WxLoginConstant.GRANT_TYPE, WxLoginConstant.AUTHORIZATION_CODE);

        // 调用Http
        String json = HttpClientUtil.doGet(WxLoginConstant.WX_LOGIN, map);

        JSONObject jsonObject = JSONObject.parseObject(json);
        return jsonObject.getString("openid");
    }
}
