package com.example.backstage.dto.response;

/**
 * 题目统计响应
 */
public class QuestionStatisticsResponse {
    private Long total;
    private Long easyCount;
    private Long mediumCount;
    private Long hardCount;
    private Double trend;

    public QuestionStatisticsResponse() {}

    public QuestionStatisticsResponse(Long total, Long easyCount, Long mediumCount, Long hardCount, Double trend) {
        this.total = total;
        this.easyCount = easyCount;
        this.mediumCount = mediumCount;
        this.hardCount = hardCount;
        this.trend = trend;
    }

    public Long getTotal() { return total; }
    public void setTotal(Long total) { this.total = total; }
    public Long getEasyCount() { return easyCount; }
    public void setEasyCount(Long easyCount) { this.easyCount = easyCount; }
    public Long getMediumCount() { return mediumCount; }
    public void setMediumCount(Long mediumCount) { this.mediumCount = mediumCount; }
    public Long getHardCount() { return hardCount; }
    public void setHardCount(Long hardCount) { this.hardCount = hardCount; }
    public Double getTrend() { return trend; }
    public void setTrend(Double trend) { this.trend = trend; }
}
