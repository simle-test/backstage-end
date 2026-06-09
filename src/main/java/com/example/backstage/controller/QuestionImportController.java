package com.example.backstage.controller;

import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.dto.QuestionImportRequest;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.service.AIService;
import com.example.backstage.service.DocumentParserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/questions")
public class QuestionImportController {

    private static final Logger log = LoggerFactory.getLogger(QuestionImportController.class);

    private final DocumentParserService documentParserService;
    private final AIService aiService;

    public QuestionImportController(DocumentParserService documentParserService, AIService aiService) {
        this.documentParserService = documentParserService;
        this.aiService = aiService;
    }

    /**
     * 上传并解析文档
     */
    @PostMapping("/parse")
    public ResponseEntity<ApiResponse<Map<String, Object>>> parseDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("category") String category) {

        try {
            log.info("开始解析文档: {}, 分类: {}", file.getOriginalFilename(), category);

            // 解析文档
            List<ParsedQuestionDTO> questions = documentParserService.parseDocument(file, category);

            // 生成答案和解析
            aiService.generateAnswersAndAnalysis(questions);

            // 统计题目类型
            Map<String, Integer> stats = new HashMap<>();
            stats.put("total", questions.size());
            stats.put("single_choice", 0);
            stats.put("multiple_choice", 0);
            stats.put("fill_blank", 0);
            stats.put("true_false", 0);
            stats.put("essay", 0);

            for (ParsedQuestionDTO question : questions) {
                String type = question.getType();
                stats.put(type, stats.getOrDefault(type, 0) + 1);
            }

            Map<String, Object> result = new HashMap<>();
            result.put("questions", questions);
            result.put("stats", stats);

            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            log.error("解析文档失败", e);
            return ResponseEntity.badRequest()
                    .body(ApiResponse.error("解析文档失败: " + e.getMessage()));
        }
    }

    /**
     * 导入题目到数据库
     */
    @PostMapping("/import")
    public ResponseEntity<ApiResponse<String>> importQuestions(
            @RequestBody QuestionImportRequest request) {

        // TODO: 实现题目导入逻辑
        // 1. 根据去重方式检查重复题目
        // 2. 保存题目到数据库
        // 3. 返回导入结果

        return ResponseEntity.ok(ApiResponse.success("导入成功"));
    }
}