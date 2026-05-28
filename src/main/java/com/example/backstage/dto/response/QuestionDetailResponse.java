package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目详情响应
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionDetailResponse {
    private Integer id;
    private String title;
    private String content;
    private List<String> options;
    private String answer;
    private String difficulty;
    private String difficultyText;
    private String category;
    private String analysis;
    private Double passRate;
    private String status;
    private String statusText;
    private String createTime;
    private String updateTime;
}
