package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 题目统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionStatisticsResponse {
    private Long total;
    private Long easyCount;
    private Long mediumCount;
    private Long hardCount;
    private Double trend;
}
