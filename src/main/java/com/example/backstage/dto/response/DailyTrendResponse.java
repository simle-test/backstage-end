package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 每日趋势响应项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DailyTrendResponse {
    private String day;
    private Long value;
}
