package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 综合统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class StatisticsResponse {
    private Long totalQuestions;
    private Long totalSubmissions;
    private Long totalUsers;
    private Double avgPassRate;
}
