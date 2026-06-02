package com.example.backstage.controller;

import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.dto.response.UserListResponse;
import com.example.backstage.dto.response.UserStatisticsResponse;
import com.example.backstage.entity.User;
import com.example.backstage.service.UserService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 用户管理控制器
 */
@RestController
@RequestMapping("/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * 获取用户列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<UserListResponse>> getUserList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String role) {
        UserListResponse response = userService.getUserList(page, size, keyword, role);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取用户统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<UserStatisticsResponse>> getUserStatistics() {
        UserStatisticsResponse response = userService.getUserStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 添加用户
     */
    @PostMapping
    public ResponseEntity<ApiResponse<User>> addUser(@RequestBody User user) {
        User newUser = userService.addUser(user);
        return ResponseEntity.ok(ApiResponse.success("添加成功", newUser));
    }

    /**
     * 获取当前登录用户信息
     */
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<User>> getCurrentUser(@RequestAttribute("user") User user) {
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 根据ID获取用户信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> getUserById(@PathVariable Long id) {
        User user = userService.findById(id);
        return ResponseEntity.ok(ApiResponse.success(user));
    }

    /**
     * 更新用户信息
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<User>> updateUser(@PathVariable Long id, @RequestBody User user) {
        User updatedUser = userService.updateUser(id, user);
        return ResponseEntity.ok(ApiResponse.success("更新成功", updatedUser));
    }

    /**
     * 删除用户
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> deleteUser(@PathVariable Long id) {
        userService.deleteUser(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功", null));
    }
}
