package com.example.backstage.service;

import com.example.backstage.dto.request.CreateQuestionRequest;
import com.example.backstage.dto.request.UpdateQuestionRequest;
import com.example.backstage.dto.response.QuestionDetailResponse;
import com.example.backstage.dto.response.QuestionListResponse;
import com.example.backstage.dto.response.QuestionStatisticsResponse;

/**
 * 题目服务接口
 */
public interface QuestionService {

    /**
     * 获取题目列表
     */
    QuestionListResponse getQuestionList(Integer page, Integer size, String keyword, String difficulty, String category);

    /**
     * 获取题目统计
     */
    QuestionStatisticsResponse getQuestionStatistics();

    /**
     * 创建题目
     */
    void createQuestion(CreateQuestionRequest request);

    /**
     * 更新题目
     */
    void updateQuestion(Integer id, UpdateQuestionRequest request);

    /**
     * 删除题目
     */
    void deleteQuestion(Integer id);

    /**
     * 获取题目详情
     */
    QuestionDetailResponse getQuestionDetail(Integer id);
}
