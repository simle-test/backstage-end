package com.example.backstage.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 题目列表项
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class QuestionListItem {
    private Integer id;
    private String questionId;
    private String title;
    private String content;
    private Object options;
    private String questionContent;
    private String answer;
    private String analysis;
    private String difficulty;
    private String difficultyText;
    private String category;
    private String categoryText;
    private Integer bankId;
    private String bankName;
    private String createdAt;
}
