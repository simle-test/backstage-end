
package com.example.backstage.service.impl;

import com.example.backstage.dto.request.LoginRequest;
import com.example.backstage.dto.request.RegisterRequest;
import com.example.backstage.dto.response.*;
import com.example.backstage.entity.User;
import com.example.backstage.entity.UsersEntity;
import com.example.backstage.repository.UserRepository;
import com.example.backstage.repository.UsersRepository;
import com.example.backstage.service.UserService;
import com.example.backstage.util.JwtUtil;
import org.springframework.context.ApplicationContext;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 用户服务实现类
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final UsersRepository usersRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final ApplicationContext applicationContext;

    public UserServiceImpl(UserRepository userRepository, UsersRepository usersRepository, PasswordEncoder passwordEncoder, 
                          JwtUtil jwtUtil, ApplicationContext applicationContext) {
        this.userRepository = userRepository;
        this.usersRepository = usersRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.applicationContext = applicationContext;
    }
    
    private AuthenticationManager authenticationManager;
    
    private AuthenticationManager getAuthenticationManager() {
        if (authenticationManager == null) {
            authenticationManager = applicationContext.getBean(AuthenticationManager.class);
        }
        return authenticationManager;
    }

    private static final Map<String, String> ROLE_MAP = new HashMap<>();
    static {
        ROLE_MAP.put("ROLE_USER", "普通用户");
        ROLE_MAP.put("ROLE_ADMIN", "管理员");
        ROLE_MAP.put("ROLE_VIP", "VIP用户");
    }

    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("active", "正常");
        STATUS_MAP.put("inactive", "未激活");
        STATUS_MAP.put("banned", "禁用");
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        Authentication authentication = getAuthenticationManager().authenticate(
                new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword())
        );

        User user = (User) authentication.getPrincipal();
        String token = jwtUtil.generateToken(user);

        LoginResponse.UserInfo userInfo = new LoginResponse.UserInfo(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getPhone(),
                user.getRole()
        );

        return new LoginResponse(token, jwtUtil.getExpiration(), userInfo);
    }

    @Override
    @Transactional
    public User register(RegisterRequest request) {
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        User user = new User();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setEmail(request.getEmail());
        user.setPhone(request.getPhone());
        user.setRole("ROLE_USER");
        user.setEnabled(true);
        user.setStatus("active");

        return userRepository.save(user);
    }

    @Override
    public User findByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException("用户不存在: " + username));
    }

    @Override
    public User findById(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("用户不存在: " + id));
    }

    @Override
    public UserListResponse getUserList(Integer page, Integer size, String keyword, String role) {
        // 从users表查询用户
        List<Object[]> userDataList = usersRepository.findAllUsersNative();
        
        // 过滤关键字
        final String finalKeyword = (keyword == null) ? "" : keyword.toLowerCase();
        List<Object[]> filteredList = userDataList.stream()
            .filter(row -> {
                String username = row.length > 1 && row[1] != null ? row[1].toString().toLowerCase() : "";
                return username.contains(finalKeyword);
            })
            .collect(Collectors.toList());
        
        // 分页处理
        int totalElements = filteredList.size();
        int start = (page - 1) * size;
        int end = Math.min(start + size, totalElements);
        List<Object[]> pageList = start < totalElements ? filteredList.subList(start, end) : new ArrayList<>();
        
        // 转换为UserListItem
        List<UserListItem> list = pageList.stream()
            .map(this::convertToListItemFromUsers)
            .collect(Collectors.toList());
        
        return new UserListResponse(list, (long) totalElements, page, size);
    }

    @Override
    public UserStatisticsResponse getUserStatistics() {
        long total = usersRepository.countUsersNative();
        long active = total; // 默认全部活跃
        long todayNew = 0;
        
        return new UserStatisticsResponse(total, active, todayNew, 85.0);
    }
    
    /**
     * 从users表数据转换为UserListItem
     */
    private UserListItem convertToListItemFromUsers(Object[] row) {
        Integer id = row.length > 0 && row[0] != null ? ((Number) row[0]).intValue() : 0;
        String username = row.length > 1 && row[1] != null ? row[1].toString() : "";
        String nickname = row.length > 2 && row[2] != null ? row[2].toString() : "";
        
        String displayName = nickname != null && !nickname.isEmpty() ? nickname : username;
        
        return new UserListItem(
            id.longValue(),
            displayName,
            null, // email
            "user",
            "普通用户",
            null, // practiceCount
            null, // passRate
            "active",
            "活跃",
            null // joinDate
        );
    }

    @Override
    @Transactional
    public User addUser(User user) {
        if (userRepository.existsByUsername(user.getUsername())) {
            throw new RuntimeException("用户名已存在");
        }

        if (userRepository.existsByEmail(user.getEmail())) {
            throw new RuntimeException("邮箱已存在");
        }

        user.setPassword(passwordEncoder.encode(user.getPassword()));
        if (user.getRole() == null) {
            user.setRole("ROLE_USER");
        }
        if (user.getStatus() == null) {
            user.setStatus("active");
        }
        user.setEnabled(true);

        return userRepository.save(user);
    }

    @Override
    @Transactional
    public User updateUser(Long id, User user) {
        User existingUser = findById(id);
        
        if (user.getUsername() != null) {
            existingUser.setUsername(user.getUsername());
        }
        
        if (user.getEmail() != null && !user.getEmail().equals(existingUser.getEmail())) {
            if (userRepository.existsByEmail(user.getEmail())) {
                throw new RuntimeException("邮箱已被使用");
            }
            existingUser.setEmail(user.getEmail());
        }
        
        if (user.getPhone() != null) {
            existingUser.setPhone(user.getPhone());
        }
        
        if (user.getRole() != null) {
            existingUser.setRole(user.getRole());
        }
        
        if (user.getStatus() != null) {
            existingUser.setStatus(user.getStatus());
        }
        
        if (user.getAvatar() != null) {
            existingUser.setAvatar(user.getAvatar());
        }
        
        return userRepository.save(existingUser);
    }

    @Override
    @Transactional
    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new RuntimeException("用户不存在: " + id);
        }
        userRepository.deleteById(id);
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return findByUsername(username);
    }

    private UserListItem convertToListItem(User user) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        String joinDate = user.getJoinDate() != null ? sdf.format(user.getJoinDate()) : "";
        
        return new UserListItem(
            user.getId(),
            user.getUsername(),
            user.getEmail(),
            user.getRole(),
            ROLE_MAP.getOrDefault(user.getRole(), user.getRole()),
            user.getPracticeCount() != null ? user.getPracticeCount().longValue() : 0L,
            user.getPassRate() != null ? user.getPassRate() : 0.0,
            user.getStatus(),
            STATUS_MAP.getOrDefault(user.getStatus(), user.getStatus()),
            joinDate
        );
    }
}
