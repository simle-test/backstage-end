package com.example.backstage.dto.response;

/**
 * 综合统计响应
 */
public class StatisticsResponse {
    private Long totalQuestions;
    private Long totalSubmissions;
    private Long totalUsers;
    private Double avgPassRate;

    public StatisticsResponse() {}

    public StatisticsResponse(Long totalQuestions, Long totalSubmissions, Long totalUsers, Double avgPassRate) {
        this.totalQuestions = totalQuestions;
        this.totalSubmissions = totalSubmissions;
        this.totalUsers = totalUsers;
        this.avgPassRate = avgPassRate;
    }

    // Getters and Setters
    public Long getTotalQuestions() { return totalQuestions; }
    public void setTotalQuestions(Long totalQuestions) { this.totalQuestions = totalQuestions; }
    public Long getTotalSubmissions() { return totalSubmissions; }
    public void setTotalSubmissions(Long totalSubmissions) { this.totalSubmissions = totalSubmissions; }
    public Long getTotalUsers() { return totalUsers; }
    public void setTotalUsers(Long totalUsers) { this.totalUsers = totalUsers; }
    public Double getAvgPassRate() { return avgPassRate; }
    public void setAvgPassRate(Double avgPassRate) { this.avgPassRate = avgPassRate; }
}