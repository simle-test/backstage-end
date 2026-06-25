package com.example.backstage.service.impl;

import com.example.backstage.dto.response.*;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.repository.UserDataRepository;
import com.example.backstage.repository.UserPracticeRepository;
import com.example.backstage.repository.UserRepository;
import com.example.backstage.repository.UsersRepository;
import com.example.backstage.repository.ProblemsDetailRepository;
import com.example.backstage.service.StatisticsService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserPracticeRepository userPracticeRepository;
    private final UserDataRepository userDataRepository;
    private final UsersRepository usersRepository;
    private final ProblemsDetailRepository problemsDetailRepository;

    public StatisticsServiceImpl(QuestionRepository questionRepository, UserRepository userRepository, 
                                UserPracticeRepository userPracticeRepository, UserDataRepository userDataRepository,
                                UsersRepository usersRepository, ProblemsDetailRepository problemsDetailRepository) {
        this.questionRepository = questionRepository;
        this.userRepository = userRepository;
        this.userPracticeRepository = userPracticeRepository;
        this.userDataRepository = userDataRepository;
        this.usersRepository = usersRepository;
        this.problemsDetailRepository = problemsDetailRepository;
    }

    @Override
    public StatisticsResponse getStatistics() {
        long totalQuestions = questionRepository.count();
        
        long totalSubmissions = 0;
        List<Object[]> userDataList = userDataRepository.findAllWeekDataWithTodayNumber();
        for (Object[] row : userDataList) {
            if (row[1] != null) {
                totalSubmissions += ((Number) row[1]).longValue();
            }
            if (row[0] != null) {
                String weekDataStr = row[0].toString();
                weekDataStr = weekDataStr.replace("{", "").replace("}", "");
                String[] parts = weekDataStr.split(",");
                for (String part : parts) {
                    try {
                        totalSubmissions += Long.parseLong(part.trim());
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
            
        long totalUsers = userRepository.count();
        double avgPassRate = 70.0;
        
        return new StatisticsResponse(totalQuestions, totalSubmissions, totalUsers, avgPassRate);
    }

    @Override
    public List<DailyTrendResponse> getDailyTrend(Integer days) {
        List<DailyTrendResponse> trends = new ArrayList<>();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        
        LocalDate today = LocalDate.now();
        
        Map<String, Long> dateCountMap = new HashMap<>();
        
        List<Object[]> userDataList = userDataRepository.findAllWeekDataWithTodayNumber();
        for (Object[] row : userDataList) {
            if (row[0] != null) {
                String weekDataStr = row[0].toString();
                weekDataStr = weekDataStr.replace("{", "").replace("}", "");
                String[] parts = weekDataStr.split(",");
                for (int i = 0; i < parts.length && i < days; i++) {
                    try {
                        int count = Integer.parseInt(parts[i].trim());
                        LocalDate date = today.minusDays(parts.length - 1 - i);
                        String dateStr = date.format(formatter);
                        dateCountMap.merge(dateStr, (long) count, Long::sum);
                    } catch (NumberFormatException e) {
                    }
                }
            }
        }
        
        for (int i = days - 1; i >= 0; i--) {
            String dateStr = today.minusDays(i).format(formatter);
            Long count = dateCountMap.getOrDefault(dateStr, 0L);
            trends.add(new DailyTrendResponse(dateStr, count));
        }
        
        return trends;
    }

    @Override
    public CategoryScoreResponse getCategoryScores() {
        Map<String, Double> categoryRateMap = new HashMap<>();
        
        List<Object[]> categoryData = problemsDetailRepository.countByCategoryName();
        for (Object[] row : categoryData) {
            String categoryName = row[0] != null ? row[0].toString() : "";
            long totalCount = row[1] != null ? ((Number) row[1]).longValue() : 0;
            long correctCount = row[2] != null ? ((Number) row[2]).longValue() : 0;
            
            double rate = totalCount > 0 ? (correctCount * 100.0 / totalCount) : 0.0;
            categoryRateMap.put(categoryName, rate);
        }
        
        double politicalTheory = categoryRateMap.getOrDefault("政治理论", 0.0);
        double quantityRelation = categoryRateMap.getOrDefault("数量关系", 0.0);
        double materialAnalysis = categoryRateMap.getOrDefault("资料分析", 0.0);
        double commonSenseJudgment = categoryRateMap.getOrDefault("常识判断", 0.0);
        double logicalJudgment = categoryRateMap.getOrDefault("判断推理", 0.0);
        double languageUnderstanding = categoryRateMap.getOrDefault("言语理解", 0.0);
        
        return new CategoryScoreResponse(politicalTheory, quantityRelation, materialAnalysis,
                commonSenseJudgment, logicalJudgment, languageUnderstanding);
    }

    @Override
    public List<RankingResponse> getRanking(Integer limit) {
        List<RankingResponse> rankings = new ArrayList<>();
        
        List<Object[]> userDataList = usersRepository.findAllUsersNative();
        
        java.util.List<UserRankData> userList = new java.util.ArrayList<>();
        for (Object[] row : userDataList) {
            UserRankData data = new UserRankData();
            if (row.length > 0) data.setId(row[0] != null ? ((Number) row[0]).intValue() : 0);
            if (row.length > 1) data.setUsername(row[1] != null ? row[1].toString() : "");
            if (row.length > 2) data.setNickname(row[2] != null ? row[2].toString() : "");
            if (row.length > 3) data.setAvatar(row[3] != null ? row[3].toString() : "");
            userList.add(data);
        }
        
        java.util.Map<Integer, Integer> userFinishedMap = new java.util.HashMap<>();
        for (UserRankData user : userList) {
            if (user.getId() != null && user.getId() > 0) {
                userDataRepository.findFinishedById(user.getId()).ifPresent(finished -> {
                    userFinishedMap.put(user.getId(), finished);
                });
            }
        }
        
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