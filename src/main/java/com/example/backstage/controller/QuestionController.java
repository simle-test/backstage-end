package com.example.backstage.controller;

import com.example.backstage.dto.request.CreateQuestionRequest;
import com.example.backstage.dto.request.UpdateQuestionRequest;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.dto.response.QuestionDetailResponse;
import com.example.backstage.dto.response.QuestionListResponse;
import com.example.backstage.dto.response.QuestionStatisticsResponse;
import com.example.backstage.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 题目管理控制器
 */
@RestController
@RequestMapping("/questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * 获取题目列表
     */
    @GetMapping
    public ResponseEntity<ApiResponse<QuestionListResponse>> getQuestionList(
            @RequestParam(defaultValue = "1") Integer page,
            @RequestParam(defaultValue = "10") Integer size,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String category) {
        QuestionListResponse response = questionService.getQuestionList(page, size, keyword, difficulty, category);
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 获取题目统计
     */
    @GetMapping("/statistics")
    public ResponseEntity<ApiResponse<QuestionStatisticsResponse>> getQuestionStatistics() {
        QuestionStatisticsResponse response = questionService.getQuestionStatistics();
        return ResponseEntity.ok(ApiResponse.success(response));
    }

    /**
     * 添加题目
     */
    @PostMapping
    public ResponseEntity<ApiResponse<String>> createQuestion(@RequestBody CreateQuestionRequest request) {
        questionService.createQuestion(request);
        return ResponseEntity.ok(ApiResponse.success("添加成功"));
    }

    /**
     * 更新题目
     */
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> updateQuestion(
            @PathVariable Integer id,
            @RequestBody UpdateQuestionRequest request) {
        questionService.updateQuestion(id, request);
        return ResponseEntity.ok(ApiResponse.success("更新成功"));
    }

    /**
     * 删除题目
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<String>> deleteQuestion(@PathVariable Integer id) {
        questionService.deleteQuestion(id);
        return ResponseEntity.ok(ApiResponse.success("删除成功"));
    }

    /**
     * 获取题目详情
     */
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<QuestionDetailResponse>> getQuestionDetail(@PathVariable Integer id) {
        QuestionDetailResponse response = questionService.getQuestionDetail(id);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}