package com.example.backstage.dto.response;

/**
 * 用户统计响应
 */
public class UserStatisticsResponse {
    private Long total;
    private Long active;
    private Long todayNew;
    private Double retentionRate;

    public UserStatisticsResponse() {}

    public UserStatisticsResponse(Long total, Long active, Long todayNew, Double retentionRate) {
        this.total = total;
        this.active = active;
        this.todayNew = todayNew;
        this.retentionRate = retentionRate;
    }

    // Getters and Setters
    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getActive() { return active; }
    public void setActive(Long active) { this.active = active; }
    public Long getTodayNew() { return todayNew; }
    public void setTodayNew(Long todayNew) { this.todayNew = todayNew; }
    public Double getRetentionRate() { return retentionRate; }
    public void setRetentionRate(Double retentionRate) { this.retentionRate = retentionRate; }
}