package com.example.backstage.service.impl;

import com.example.backstage.dto.request.LoginRequest;
import com.example.backstage.dto.response.*;
import com.example.backstage.entity.User;
import com.example.backstage.repository.UserRepository;
import com.example.backstage.service.UserService;
import com.example.backstage.util.JwtUtil;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    public UserServiceImpl(UserRepository userRepository, JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.jwtUtil = jwtUtil;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new RuntimeException("用户名或密码错误"));
        
        if (!request.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("用户名或密码错误");
        }

        String token = jwtUtil.generateToken(user);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getUserId().longValue(),
                user.getUsername(),
                "",
                "",
                "ROLE_USER"
        );

        return new LoginResponse(token, jwtUtil.getExpiration(), userInfo);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id.intValue())
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    @Override
    public UserListResponse getUserList(Integer page, Integer size, String keyword, String role) {
        List<User> allUsers = userRepository.findAll();
        
        List<UserListItem> filteredList = allUsers.stream()
            .filter(user -> {
                if (keyword != null && !keyword.isEmpty()) {
                    String kw = keyword.toLowerCase();
                    return user.getUsername().toLowerCase().contains(kw);
                }
                return true;
            })
            .map(this::convertToListItem)
            .collect(Collectors.toList());
        
        int total = filteredList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, total);
        
        List<UserListItem> pagedList;
        if (start >= total) {
            pagedList = new ArrayList<>();
        } else {
            pagedList = new ArrayList<>(filteredList.subList(start, end));
        }
        
        return new UserListResponse(pagedList, (long) total, page, size);
    }

    @Override
    public UserStatisticsResponse getUserStatistics() {
        long total = userRepository.count();
        long active = total;
        long todayNew = 0;
        
        return new UserStatisticsResponse(total, active, todayNew, 85.0);
    }

    @Override
    @Transactional
    public User addUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }
        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = findById(id);
        
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        
        if (user.getWechatOpenid() != null) {
            existingUser.setWechatOpenid(user.getWechatOpenid());
        }
        
        if (user.getPassword() != null) {
            existingUser.setPassword(user.getPassword());
        }
        
        if (user.getAvatarUrl() != null) {
            existingUser.setAvatarUrl(user.getAvatarUrl());
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id.intValue())) {
            throw new RuntimeException("用户不存在: " + id);
        }
        userRepository.deleteById(id.intValue());
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username);
    }

    private UserListItem convertToListItem(User user) {
        String avatarStr = "U";
        if (user.getUsername() != null && !user.getUsername().isEmpty()) {
            avatarStr = String.valueOf(user.getUsername().charAt(0)).toUpperCase();
        }
        
        String colorStr = "#3498db";
        if (user.getUserId() != null) {
            String hex = Integer.toHexString((user.getUserId() * 0x33) % 0xFFFFFF);
            while (hex.length() < 6) {
                hex = "0" + hex;
            }
            colorStr = "#" + hex.substring(0, 6);
        }
        
        UserListItem item = new UserListItem();
        item.setId(user.getUserId().longValue());
        item.setUsername(user.getUsername() != null ? user.getUsername() : "");
        item.setEmail("");
        item.setRole("ROLE_USER");
        item.setRoleText("普通用户");
        item.setPracticeCount(0L);
        item.setPassRate(0.0);
        item.setJoinDate("");
        item.setStatus("active");
        item.setStatusText("正常");
        item.setAvatar(avatarStr);
        item.setColor(colorStr);
        
        return item;
    }

    @Override
    public User register(com.example.backstage.dto.request.RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(request.getPassword());

        return userRepository.save(user);
    }
}
