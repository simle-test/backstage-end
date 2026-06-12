package com.example.backstage.dto.response;

/**
 * 每日趋势响应项
 */
public class DailyTrendResponse {
    private String day;
    private Long value;

    public DailyTrendResponse() {}

    public DailyTrendResponse(String day, Long value) {
        this.day = day;
        this.value = value;
    }

    // Getters and Setters
    public String getDay() { return day; }
    public void setDay(String day) { this.day = day; }
    public Long getValue() { return value; }
    public void setValue(Long value) { this.value = value; }
}