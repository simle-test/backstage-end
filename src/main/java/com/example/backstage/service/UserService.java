
package com.example.backstage.service;

import com.example.backstage.dto.request.LoginRequest;
import com.example.backstage.dto.request.RegisterRequest;
import com.example.backstage.dto.response.LoginResponse;
import com.example.backstage.entity.User;
import org.springframework.security.core.userdetails.UserDetailsService;

/**
 * 用户服务接口
 */
public interface UserService extends UserDetailsService {

    /**
     * 用户登录
     */
    LoginResponse login(LoginRequest request);

    /**
     * 用户注册
     */
    User register(RegisterRequest request);

    /**
     * 根据用户名获取用户
     */
    User findByUsername(String username);

    /**
     * 根据ID获取用户
     */
    User findById(Long id);

    /**
     * 更新用户信息
     */
    User updateUser(Long id, User user);

    /**
     * 删除用户
     */
    void deleteUser(Long id);
}
