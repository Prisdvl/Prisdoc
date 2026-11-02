package com.javaee.user.controller;

import com.javaee.common.model.Result;
import com.javaee.user.dto.LoginDTO;
import com.javaee.user.dto.RegisterDTO;
import com.javaee.user.entity.User;
import com.javaee.user.service.UserService;
import com.javaee.user.vo.LoginVO;
import com.javaee.user.vo.UserVO;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口控制器，提供登录、注册、信息查询、令牌刷新与登出接口。
 *
 * @author qxk
 */
@Slf4j
@RestController
@RequestMapping("/api/users")
@Tag(name = "用户管理", description = "用户登录、注册、信息管理等接口")
public class UserController {

    @Autowired
    private UserService userService;

    /**
     * 校验用户名与密码并签发访问令牌与刷新令牌。
     *
     * @param loginDTO 登录请求参数，包含用户名与密码
     * @return 登录结果，含访问令牌、刷新令牌与用户信息
     */
    @PostMapping("/login")
    @Operation(summary = "用户登录", description = "根据用户名和密码进行登录，返回访问令牌和刷新令牌")
    public Result<LoginVO> login(@RequestBody LoginDTO loginDTO) {
        log.info("用户登录: {}", loginDTO.getUsername());
        LoginVO loginVO = userService.login(loginDTO);
        return Result.success(loginVO);
    }

    /**
     * 注册新用户并返回用户信息。
     *
     * @param registerDTO 注册请求参数，包含用户名、密码、邮箱与手机号
     * @return 新建用户的展示信息
     */
    @PostMapping("/register")
    @Operation(summary = "用户注册", description = "创建新用户，返回用户信息")
    public Result<UserVO> register(@RequestBody RegisterDTO registerDTO) {
        log.info("用户注册: {}", registerDTO.getUsername());
        UserVO userVO = userService.register(registerDTO);
        return Result.success(userVO);
    }

    /**
     * 按用户 ID 查询用户信息。
     *
     * @param id 用户 ID
     * @return 用户的展示信息
     */
    @GetMapping("/{id:\\d+}")
    @Operation(summary = "获取用户信息", description = "根据用户ID获取用户详细信息")
    public Result<UserVO> getUserById(@Parameter(description = "用户ID") @PathVariable Long id) {
        log.info("获取用户信息: {}", id);
        UserVO userVO = userService.getUserById(id);
        return Result.success(userVO);
    }

    /**
     * 按用户名查询用户，供文档协作授权时查找协作者。
     *
     * @param username 用户名
     * @return 用户的展示信息；用户不存在时抛出业务异常
     */
    @GetMapping("/lookup")
    @Operation(summary = "根据用户名查询用户", description = "用于文档协作授权时查找协作者")
    public Result<UserVO> getUserByUsername(@Parameter(description = "用户名") @RequestParam String username) {
        log.info("根据用户名查询用户: {}", username);
        User user = userService.getUserByUsername(username);
        if (user == null) {
            throw new com.javaee.common.exception.BusinessException(com.javaee.common.constant.ErrorCodeEnum.USER_NOT_FOUND);
        }
        UserVO userVO = new UserVO();
        BeanUtils.copyProperties(user, userVO);
        return Result.success(userVO);
    }

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 新签发的访问令牌
     */
    @PostMapping("/refresh")
    @Operation(summary = "刷新令牌", description = "使用刷新令牌获取新的访问令牌")
    public Result<String> refreshToken(@Parameter(description = "刷新令牌") @RequestParam String refreshToken) {
        log.info("刷新令牌");
        String accessToken = userService.refreshToken(refreshToken);
        return Result.success(accessToken);
    }

    /**
     * 用户登出，服务端不保存会话状态。
     *
     * @return 空数据的成功响应
     */
    @PostMapping("/logout")
    @Operation(summary = "用户登出", description = "用户登出操作")
    public Result<Void> logout() {
        log.info("用户登出");
        // 前端清除本地存储的令牌
        return Result.success();
    }
}
