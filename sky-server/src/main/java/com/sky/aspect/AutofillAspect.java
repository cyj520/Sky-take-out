package com.sky.aspect;

/**
 * @author: ChuYangjie
 * @date: 2024/3/23 21:40
 * @version: 1.0
 */

import com.sky.annotation.Autofill;
import com.sky.constant.AutoFillConstant;
import com.sky.context.BaseContext;
import com.sky.enumeration.OperationType;
import io.swagger.models.auth.In;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.time.LocalDateTime;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * 自定义切面，实现公共字段自动填充逻辑
 */
@Aspect
@Component
@Slf4j
public class AutofillAspect {

    // 指定切入点
    @Pointcut("execution(* com.sky.mapper.*.*(..)) && @annotation(com.sky.annotation.Autofill)")
    public void autofillPointcut(){}

    /**
     * 前置通知，在通知中进行公共字段赋值
     */
    @Before("autofillPointcut()")
    public void autofill(JoinPoint joinPoint) {
        log.info("开始进行公共字段填充");
        // 获取当前被拦截方法的操作类型(INSERT/UPDATE)
        MethodSignature signature = (MethodSignature) joinPoint.getSignature();
        Autofill annotation = signature.getMethod().getAnnotation(Autofill.class);
        OperationType operationType = annotation.value();

        // 获取方法中的参数，实体对象
        Object[] args = joinPoint.getArgs();
        if (args == null || args.length == 0) {
            return;
        }
        Object entity = args[0]; // 默认实体对象写在方法的第一个位置(规范)
        // 通过反射，赋值
        LocalDateTime now = LocalDateTime.now();
        Long currentId = BaseContext.getCurrentId();
        if (operationType == OperationType.INSERT) {
            // 更新createUser,createTime,updateUser,updateTime
            try {
                Method setCreateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_TIME, LocalDateTime.class);
                Method setCreateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_CREATE_USER, Long.class);
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setCreateTime.invoke(entity, now);
                setCreateUser.invoke(entity, currentId);
                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        } else if (operationType == OperationType.UPDATE){
            try {
                Method setUpdateTime = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_TIME, LocalDateTime.class);
                Method setUpdateUser = entity.getClass().getDeclaredMethod(AutoFillConstant.SET_UPDATE_USER, Long.class);

                setUpdateTime.invoke(entity, now);
                setUpdateUser.invoke(entity, currentId);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        }
    }
}
