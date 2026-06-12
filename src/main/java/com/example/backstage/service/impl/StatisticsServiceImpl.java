package com.example.backstage.service.impl;

import com.example.backstage.dto.response.*;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.repository.UserDataRepository;
import com.example.backstage.repository.UserPracticeRepository;
import com.example.backstage.repository.UserRepository;
import com.example.backstage.repository.UsersRepository;
import com.example.backstage.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

/**
 * 统计服务实现
 */
@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserPracticeRepository userPracticeRepository;
    private final UserDataRepository userDataRepository;
    private final UsersRepository usersRepository;

    public StatisticsServiceImpl(QuestionRepository questionRepository, UserRepository userRepository, 
                                UserPracticeRepository userPracticeRepository, UserDataRepository userDataRepository,
                                UsersRepository usersRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.userPracticeRepository = userPracticeRepository;
        this.userDataRepository = userDataRepository;
        this.usersRepository = usersRepository;
    }

    @Override
    public StatisticsResponse getStatistics() {
        long totalQuestions = questionRepository.count();
        long totalSubmissions = userPracticeRepository.count();
        long totalUsers = userRepository.count();
        double avgPassRate = 70.0;
        
        return new StatisticsResponse(totalQuestions, totalSubmissions, totalUsers, avgPassRate);
    }

    @Override
    public List<DailyTrendResponse> getDailyTrend(Integer days) {
        List<DailyTrendResponse> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDateTime startDate = LocalDateTime.now().minusDays(days);
        for (int i = 0; i < days; i++) {
            trends.add(new DailyTrendResponse(startDate.plusDays(i).format(formatter), 10L + i * 5));
        }
        
        return trends;
    }

    @Override
    public CategoryScoreResponse getCategoryScores() {
        return new CategoryScoreResponse(80.0, 75.0, 70.0);
    }

    @Override
    public List<RankingResponse> getRanking(Integer limit) {
        List<RankingResponse> rankings = new ArrayList<>();
        
        // 使用原生SQL从users表查询所有用户
        List<Object[]> userDataList = usersRepository.findAllUsersNative();
        
        // 创建用户数据映射（存储用户信息）
        java.util.List<UserRankData> userList = new java.util.ArrayList<>();
        for (Object[] row : userDataList) {
            UserRankData data = new UserRankData();
            // 根据截图，users表至少包含id和username字段
            if (row.length > 0) data.setId(row[0] != null ? ((Number) row[0]).intValue() : 0);
            if (row.length > 1) data.setUsername(row[1] != null ? row[1].toString() : "");
            if (row.length > 2) data.setNickname(row[2] != null ? row[2].toString() : "");
            if (row.length > 3) data.setAvatar(row[3] != null ? row[3].toString() : "");
            userList.add(data);
        }
        
        // 创建用户刷题数量映射
        java.util.Map<Integer, Integer> userFinishedMap = new java.util.HashMap<>();
        for (UserRankData user : userList) {
            if (user.getId() != null && user.getId() > 0) {
                userDataRepository.findFinishedById(user.getId()).ifPresent(finished -> {
                    userFinishedMap.put(user.getId(), finished);
                });
            }
        }
        
        // 按完成题目数降序排序
        userList.sort((u1, u2) -> {
            Integer f1 = userFinishedMap.getOrDefault(u1.getId() != null ? u1.getId() : 0, 0);
            Integer f2 = userFinishedMap.getOrDefault(u2.getId() != null ? u2.getId() : 0, 0);
            return f2.compareTo(f1);
        });
        
        String[] colors = {"#4285F4", "#EA4335", "#FBBC05", "#34A853", "#FF6D01", "#46BDC6", "#7B1FA2", "#C2185B", "#0097A7", "#F57C00"};
        
        int rank = 1;
        for (UserRankData user : userList) {
            if (rank > limit) break;
            
            Integer userId = user.getId();
            Integer finished = userFinishedMap.getOrDefault(userId != null ? userId : 0, 0);
            String username = user.getUsername();
            // 优先使用nickname，没有则使用username
            String displayName = (user.getNickname() != null && !user.getNickname().isEmpty()) ? 
                user.getNickname() : username;
            String avatarChar = (displayName != null && !displayName.isEmpty()) ? 
                String.valueOf(displayName.charAt(0)) : "?";
            
            rankings.add(new RankingResponse(
                rank++,
                displayName != null && !displayName.isEmpty() ? displayName : "用户" + userId,
                avatarChar,
                colors[(rank - 2) % colors.length],
                "刷题达人",
                finished.longValue()
            ));
        }
        
        return rankings;
    }
    
    /**
     * 用户排行数据内部类
     */
    private static class UserRankData {
        private Integer id;
        private String username;
        private String nickname;
        private String avatar;
        
        public Integer getId() { return id; }
        public void setId(Integer id) { this.id = id; }
        public String getUsername() { return username; }
        public void setUsername(String username) { this.username = username; }
        public String getNickname() { return nickname; }
        public void setNickname(String nickname) { this.nickname = nickname; }
        public String getAvatar() { return avatar; }
        public void setAvatar(String avatar) { this.avatar = avatar; }
    }
}
