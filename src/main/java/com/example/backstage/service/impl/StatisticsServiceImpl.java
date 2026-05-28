package com.example.backstage.service.impl;

import com.example.backstage.dto.response.*;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.repository.UserPracticeRepository;
import com.example.backstage.repository.UserRepository;
import com.example.backstage.service.StatisticsService;
import lombok.RequiredArgsConstructor;
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
@RequiredArgsConstructor
@Slf4j
public class StatisticsServiceImpl implements StatisticsService {

    private final QuestionRepository questionRepository;
    private final UserRepository userRepository;
    private final UserPracticeRepository userPracticeRepository;

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
        
        String[] names = {"张三", "李四", "王五", "赵六", "钱七"};
        for (int i = 0; i < limit && i < names.length; i++) {
            rankings.add(new RankingResponse(
                i + 1,
                names[i],
                String.valueOf(names[i].charAt(0)),
                "#" + Integer.toHexString((0x33 + i * 0x22) % 0xFFFFFF).substring(0, 6),
                "刷题达人",
                50L + i * 20
            ));
        }
        
        return rankings;
    }
}
