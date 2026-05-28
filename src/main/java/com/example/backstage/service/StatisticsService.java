package com.example.backstage.service;

import com.example.backstage.dto.response.*;

import java.util.List;

/**
 * 统计服务接口
 */
public interface StatisticsService {

    /**
     * 获取综合统计
     */
    StatisticsResponse getStatistics();

    /**
     * 获取每日提交趋势
     */
    List<DailyTrendResponse> getDailyTrend(Integer days);

    /**
     * 获取模块得分分布
     */
    CategoryScoreResponse getCategoryScores();

    /**
     * 获取刷题排行榜
     */
    List<RankingResponse> getRanking(Integer limit);
}
