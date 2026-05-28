package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 模块得分分布响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class CategoryScoreResponse {
    private Double speech;
    private Double logic;
    private Double data;
}
