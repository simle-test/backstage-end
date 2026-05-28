package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 用户统计响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserStatisticsResponse {
    private Long total;
    private Long active;
    private Long todayNew;
    private Double retentionRate;
}
