package com.example.backstage.controller;

import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.entity.EndUser;
import com.example.backstage.repository.EndUserRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 终端用户控制器
 */
@RestController
@RequestMapping("/end-users")
public class EndUserController {

    private final EndUserRepository endUserRepository;

    public EndUserController(EndUserRepository endUserRepository) {
        this.endUserRepository = endUserRepository;
    }

    /**
     * 获取所有终端用户列表（仅显示用户名，不显示密码）
     */
    @GetMapping("/list")
    public ResponseEntity<ApiResponse<List<String>>> listUsers() {
        List<String> usernames = endUserRepository.findAll().stream()
                .map(EndUser::getUsername)
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(usernames));
    }

    /**
     * 获取用户详细信息（包括密码，用于调试）
     */
    @GetMapping("/details")
    public ResponseEntity<ApiResponse<List<Map<String, String>>>> getUserDetails() {
        List<Map<String, String>> userDetails = endUserRepository.findAll().stream()
                .map(user -> {
                    Map<String, String> map = new HashMap<>();
                    map.put("username", user.getUsername());
                    map.put("password", user.getPassword());
                    return map;
                })
                .collect(Collectors.toList());
        return ResponseEntity.ok(ApiResponse.success(userDetails));
    }
}
