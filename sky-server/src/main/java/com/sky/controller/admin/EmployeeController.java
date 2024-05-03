package com.sky.controller.admin;

import com.sky.constant.JwtClaimsConstant;
import com.sky.dto.EmployeeDTO;
import com.sky.dto.EmployeeLoginDTO;
import com.sky.dto.EmployeePageQueryDTO;
import com.sky.dto.PasswordEditDTO;
import com.sky.entity.Employee;
import com.sky.properties.JwtProperties;
import com.sky.result.PageResult;
import com.sky.result.Result;
import com.sky.service.EmployeeService;
import com.sky.utils.JwtUtil;
import com.sky.vo.EmployeeLoginVO;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * 员工管理
 */
@RestController
@RequestMapping("/admin/employee")
@Slf4j
@Api(tags = "员工相关接口")
public class EmployeeController {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private JwtProperties jwtProperties;

    @ApiOperation("员工登录")
    @PostMapping("/login")
    public Result<EmployeeLoginVO> login(@RequestBody EmployeeLoginDTO employeeLoginDTO) {
        log.info("员工登录：{}", employeeLoginDTO);

        Employee employee = employeeService.login(employeeLoginDTO);

        //登录成功后，生成jwt令牌
        Map<String, Object> claims = new HashMap<>();
        claims.put(JwtClaimsConstant.EMP_ID, employee.getId());
        String token = JwtUtil.createJWT(
                jwtProperties.getAdminSecretKey(),
                jwtProperties.getAdminTtl(),
                claims);

        EmployeeLoginVO employeeLoginVO = EmployeeLoginVO.builder()
                .id(employee.getId())
                .userName(employee.getUsername())
                .name(employee.getName())
                .token(token)
                .build();

        return Result.success(employeeLoginVO);
    }

    @ApiOperation("员工退出登录")
    @PostMapping("/logout")
    public Result<String> logout() {
        return Result.success();
    }

    @ApiOperation("新增员工")
    @PostMapping
    public Result<Employee> save(@RequestBody EmployeeDTO employeeDTO) {
        log.info("新增员工{}", employeeDTO);
        employeeService.save(employeeDTO);
        return Result.success();
    }

    @ApiOperation("分页查询")
    @GetMapping("/page")
    public Result<PageResult> page(EmployeePageQueryDTO employeePageQueryDTO) {
        log.info("分页查询，参数{}" , employeePageQueryDTO);
        PageResult pageResult = employeeService.page(employeePageQueryDTO);
        return Result.success(pageResult);
    }

    @ApiOperation("启用/禁用员工账号")
    @PostMapping("/status/{status}")
    public Result startOrStop(@PathVariable Integer status, Long id) {
        log.info("启用/禁用员工账号:{},{}", status, id);
        employeeService.startOrStop(status, id);
        return Result.success();
    }

    @ApiOperation("根据id查询员工")
    @GetMapping("/{id}")
    public Result<Employee> query(@PathVariable Long id) {
        log.info("查询员工，id：{}", id);
        return Result.success(employeeService.query(id));
    }

    @ApiOperation("根据id编辑员工")
    @PutMapping
    public Result edit(@RequestBody EmployeeDTO employeeDTO) {
        log.info("编辑员工，id{}", employeeDTO.getId());
        employeeService.edit(employeeDTO);
        return Result.success();
    }

    @ApiOperation("员工修改密码")
    @PutMapping("/editPassword")
    public Result editPassword(@RequestBody PasswordEditDTO passwordEditDTO) {
        // 修改密码应该是当前登录员工账号，但前端传回来数据含有empID，此时ID应从DTO对象中取，还是JWT令牌中取？
        // 实际前端传回的DTO数据中empID其实是空的，需要自己从JWT令牌中获取
        log.info("员工修改密码");
        employeeService.editPassword(passwordEditDTO);
        return Result.success();
    }
}
