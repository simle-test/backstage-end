package com.example.backstage.controller;

import com.example.backstage.dto.response.*;
import com.example.backstage.service.StatisticsService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 统计管理控制器
 */
@RestController
@RequestMapping("/statistics")
@RequiredArgsConstructor
public class StatisticsController {

    private final StatisticsService statisticsService;

    /**
     * 获取综合统计
     */
    @GetMapping
    public ResponseEntity<ApiResponse<StatisticsResponse>> getStatistics() {
        StatisticsResponse response = statisticsService.getStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取每日提交趋势
     */
    @GetMapping("/daily")
    public ResponseEntity<ApiResponse<List<DailyTrendResponse>>> getDailyTrend(
            @RequestParam(defaultValue = "7") Integer days) {
        List<DailyTrendResponse> response = statisticsService.getDailyTrend(days);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取模块得分分布
     */
    @GetMapping("/categories")
    public ResponseEntity<ApiResponse<CategoryScoreResponse>> getCategoryScores() {
        CategoryScoreResponse response = statisticsService.getCategoryScores();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取刷题排行榜
     */
    @GetMapping("/ranking")
    public ResponseEntity<ApiResponse<List<RankingResponse>>> getRanking(
            @RequestParam(defaultValue = "10") Integer limit) {
        List<RankingResponse> response = statisticsService.getRanking(limit);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
