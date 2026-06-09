package com.example.backstage.service;

import com.example.backstage.dto.ParsedQuestionDTO;

import java.util.List;

public interface AIService {
    /**
     * 为题目生成正确答案
     * @param question 题目信息
     * @return 正确答案
     */
    String generateAnswer(ParsedQuestionDTO question);

    /**
     * 为题目生成答案解析
     * @param question 题目信息
     * @return 答案解析
     */
    String generateAnalysis(ParsedQuestionDTO question);

    /**
     * 批量生成答案和解析
     * @param questions 题目列表
     */
    void generateAnswersAndAnalysis(List<ParsedQuestionDTO> questions);
}