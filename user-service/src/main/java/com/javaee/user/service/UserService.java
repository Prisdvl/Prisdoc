package com.javaee.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.javaee.user.dto.LoginDTO;
import com.javaee.user.dto.RegisterDTO;
import com.javaee.user.entity.User;
import com.javaee.user.vo.LoginVO;
import com.javaee.user.vo.UserVO;

/**
 * 用户服务接口，定义登录、注册、信息查询与令牌刷新能力。
 *
 * @author qxk
 */
public interface UserService extends IService<User> {
    /**
     * 校验用户凭据并签发访问令牌与刷新令牌。
     *
     * @param loginDTO 登录信息，包含用户名与明文密码
     * @return 登录结果，含访问令牌、刷新令牌与用户信息
     */
    LoginVO login(LoginDTO loginDTO);

    /**
     * 注册新用户并返回用户信息。
     *
     * @param registerDTO 注册信息，包含用户名、密码、邮箱与手机号
     * @return 新建用户的展示信息
     */
    UserVO register(RegisterDTO registerDTO);

    /**
     * 按用户名查询用户，供文档协作授权时查找协作者。
     *
     * @param username 用户名，不允许为空
     * @return 用户实体；用户不存在时返回 null
     */
    User getUserByUsername(String username);

    /**
     * 按用户 ID 查询用户信息。
     *
     * @param id 用户 ID
     * @return 用户信息；用户不存在时抛出业务异常
     */
    UserVO getUserById(Long id);

    /**
     * 使用刷新令牌换取新的访问令牌。
     *
     * @param refreshToken 刷新令牌
     * @return 新签发的访问令牌
     */
    String refreshToken(String refreshToken);
}
