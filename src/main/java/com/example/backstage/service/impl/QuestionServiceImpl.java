package com.example.backstage.service.impl;

import com.example.backstage.dto.request.CreateQuestionRequest;
import com.example.backstage.dto.request.UpdateQuestionRequest;
import com.example.backstage.dto.response.*;
import com.example.backstage.entity.Question;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.service.QuestionService;

import jakarta.persistence.EntityNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 题目服务实现
 */
@Service
public class QuestionServiceImpl implements QuestionService {

    private static final Logger log = LoggerFactory.getLogger(QuestionServiceImpl.class);

    private final QuestionRepository questionRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository) {
        this.questionRepository = questionRepository;
    }
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    private static final Map<String, String> DIFFICULTY_MAP = new HashMap<>();
    static {
        DIFFICULTY_MAP.put("easy", "简单");
        DIFFICULTY_MAP.put("medium", "中等");
        DIFFICULTY_MAP.put("hard", "困难");
    }

    private static final Map<String, String> STATUS_MAP = new HashMap<>();
    static {
        STATUS_MAP.put("active", "上线");
        STATUS_MAP.put("draft", "草稿");
        STATUS_MAP.put("published", "已发布");
    }

    private static final Map<String, String> CATEGORY_MAP = new HashMap<>();
    static {
        CATEGORY_MAP.put("language_understanding", "言语理解");
        CATEGORY_MAP.put("quantity_relation", "数量关系");
        CATEGORY_MAP.put("material_analysis", "资料分析");
        CATEGORY_MAP.put("common_sense_judgment", "常识判断");
        CATEGORY_MAP.put("logical_judgment", "判断推理");
        CATEGORY_MAP.put("political_theory", "政治理论");
    }

    @Override
    public QuestionListResponse getQuestionList(Integer page, Integer size, String keyword, String difficulty, String category) {
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.ASC, "id"));

        // 使用final变量
        final String finalKeyword = (keyword == null) ? "" : keyword;
        final String finalDifficulty = (difficulty == null) ? "" : difficulty;
        final String finalCategory = (category == null) ? "" : category;

        Page<Question> questionPage;

        // 按分类、难度、关键词筛选题目
        questionPage = questionRepository.findByCategoryIdContainingAndDifficultyContainingAndTitleContaining(
            finalCategory, finalDifficulty, finalKeyword, pageable);

        List<QuestionListItem> list = questionPage.getContent().stream()
            .map(this::convertToListItem)
            .collect(Collectors.toList());

        return new QuestionListResponse(
            list, questionPage.getTotalElements(), page, size);
    }

    @Override
    public QuestionStatisticsResponse getQuestionStatistics() {
        long total = questionRepository.count();
        long easyCount = questionRepository.countByDifficulty("easy");
        long mediumCount = questionRepository.countByDifficulty("medium");
        long hardCount = questionRepository.countByDifficulty("hard");
        
        // 计算今日新增题目数量
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        long todayCount = questionRepository.countByCreatedAtAfter(startOfDay);

        return new QuestionStatisticsResponse(total, easyCount, mediumCount, hardCount, 0.0, todayCount);
    }

    @Override
    public void createQuestion(CreateQuestionRequest request) {
        Question question = new Question();
        question.setTitle(request.getTitle());
        if (request.getQuestionContent() != null) {
            question.setQuestionContent(request.getQuestionContent());
        } else if (request.getContent() != null) {
            question.setQuestionContent(request.getContent());
        }
        question.setCorrectAnswer(request.getAnswer());
        question.setDifficulty(request.getDifficulty());
        question.setCategoryId(request.getCategory());
        question.setAnswerAnalysis(request.getAnalysis());
        if (request.getSource() != null) question.setSource(request.getSource());
        if (request.getYear() != null) question.setYear(request.getYear());
        if (request.getStatus() != null) {
            question.setStatus(request.getStatus());
        } else {
            question.setStatus("draft");
        }
        questionRepository.save(question);
    }

    @Override
    public void updateQuestion(Integer id, UpdateQuestionRequest request) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("题目不存在"));

        if (request.getTitle() != null) question.setTitle(request.getTitle());
        if (request.getQuestionContent() != null) {
            question.setQuestionContent(request.getQuestionContent());
        } else if (request.getContent() != null) {
            question.setContentText(request.getContent());
        }
        if (request.getAnswer() != null) question.setCorrectAnswer(request.getAnswer());
        if (request.getDifficulty() != null) question.setDifficulty(request.getDifficulty());
        if (request.getCategory() != null) question.setCategoryId(request.getCategory());
        if (request.getAnalysis() != null) question.setAnswerAnalysis(request.getAnalysis());
        if (request.getStatus() != null) question.setStatus(request.getStatus());
        if (request.getSource() != null) question.setSource(request.getSource());
        if (request.getYear() != null) question.setYear(request.getYear());

        questionRepository.save(question);
    }

    @Override
    public void deleteQuestion(Integer id) {
        if (!questionRepository.existsById(id)) {
            throw new EntityNotFoundException("题目不存在");
        }
        questionRepository.deleteById(id);
    }

    @Override
    public QuestionDetailResponse getQuestionDetail(Integer id) {
        Question question = questionRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("题目不存在"));

        return convertToDetailResponse(question);
    }

    private static final String IMAGE_BASE_URL = "https://www.civilservant.cloud/";

    private QuestionListItem convertToListItem(Question question) {
        // 构建完整的图片 URL
        String fullImageUrl = null;
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            fullImageUrl = IMAGE_BASE_URL + question.getImageUrl();
        }

        return new QuestionListItem(
            question.getId(),
            question.getQuestionId(),
            question.getTitle(),
            question.getContentText(),
            null,
            question.getQuestionContent(),
            question.getCorrectAnswer(),
            question.getAnswerAnalysis(),
            question.getDifficulty(),
            DIFFICULTY_MAP.getOrDefault(question.getDifficulty(), question.getDifficulty()),
            question.getCategoryId(),
            CATEGORY_MAP.getOrDefault(question.getCategoryId(), question.getCategoryName() != null ? question.getCategoryName() : question.getCategoryId()),
            null,
            null,
            question.getMaterialId(),
            fullImageUrl,
            question.getCreatedAt() != null ? question.getCreatedAt().format(FORMATTER) : ""
        );
    }

    private QuestionDetailResponse convertToDetailResponse(Question question) {
        QuestionDetailResponse response = new QuestionDetailResponse(
            question.getId(),
            question.getTitle(),
            question.getContentText(),
            Arrays.asList("A", "B", "C", "D"),
            question.getCorrectAnswer(),
            question.getDifficulty(),
            DIFFICULTY_MAP.getOrDefault(question.getDifficulty(), question.getDifficulty()),
            question.getCategoryName(),
            question.getAnswerAnalysis(),
            75.0,
            question.getStatus(),
            STATUS_MAP.getOrDefault(question.getStatus(), question.getStatus()),
            question.getCreatedAt() != null ? question.getCreatedAt().format(FORMATTER) : "",
            question.getUpdatedAt() != null ? question.getUpdatedAt().format(FORMATTER) : ""
        );
        response.setQuestionContent(question.getQuestionContent());
        response.setSource(question.getSource());
        response.setYear(question.getYear());
        response.setCategoryId(question.getCategoryId());
        
        // 设置题目图片 URL
        if (question.getImageUrl() != null && !question.getImageUrl().isEmpty()) {
            response.setImageUrl(IMAGE_BASE_URL + question.getImageUrl());
        }

        // 如果是资料分析题，设置材料信息（材料内容存储在 contentText 字段中）
        if (question.getMaterialId() != null && "material_analysis".equals(question.getCategoryId())) {
            response.setMaterialId(question.getMaterialId());
            // 材料标题可以从题目标题或分类名称中获取
            response.setMaterialTitle(question.getCategoryName() != null ? question.getCategoryName() : "资料分析");
            // 材料内容存储在 contentText 字段中
            response.setMaterialContent(question.getContentText());
        }

        return response;
    }

    @Override
    public QuestionListResponse getRecentQuestions(Integer days, Integer page, Integer size, String category) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        Pageable pageable = PageRequest.of(page - 1, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Question> questionPage;

        if (category != null && !category.isEmpty()) {
            questionPage = questionRepository.findByCategoryIdAndCreatedAtAfter(category, startTime, pageable);
        } else {
            questionPage = questionRepository.findByCreatedAtAfter(startTime, pageable);
        }

        List<QuestionListItem> list = questionPage.getContent().stream()
            .map(this::convertToListItem)
            .collect(Collectors.toList());

        return new QuestionListResponse(
            list, questionPage.getTotalElements(), page, size);
    }

    @Override
    public Object getRecentStats(Integer days) {
        LocalDateTime startTime = LocalDateTime.now().minusDays(days);
        
        Map<String, Object> result = new HashMap<>();
        List<Map<String, Object>> categoryStats = new ArrayList<>();
        
        for (Map.Entry<String, String> entry : CATEGORY_MAP.entrySet()) {
            String categoryId = entry.getKey();
            String categoryName = entry.getValue();
            
            long count = questionRepository.countByCategoryIdAndCreatedAtAfter(categoryId, startTime);
            
            // 计算较上周增长率
            LocalDateTime lastWeekStart = startTime.minusDays(days);
            LocalDateTime lastWeekEnd = startTime;
            long lastWeekCount = questionRepository.countByCategoryIdAndCreatedAtBetween(categoryId, lastWeekStart, lastWeekEnd);
            
            int growthRate = 0;
            if (lastWeekCount > 0) {
                growthRate = (int) ((count - lastWeekCount) * 100 / lastWeekCount);
            }
            
            Map<String, Object> stat = new HashMap<>();
            stat.put("categoryId", categoryId);
            stat.put("categoryName", categoryName);
            stat.put("count", count);
            stat.put("growthRate", growthRate);
            
            // 获取最新的两条题目
            Pageable topTwo = PageRequest.of(0, 2, Sort.by(Sort.Direction.DESC, "createdAt"));
            Page<Question> recentQuestions = questionRepository.findByCategoryIdAndCreatedAtAfter(categoryId, startTime, topTwo);
            
            List<Map<String, Object>> latestQuestions = new ArrayList<>();
            for (Question q : recentQuestions.getContent()) {
                Map<String, Object> qMap = new HashMap<>();
                qMap.put("time", q.getCreatedAt() != null ? q.getCreatedAt().format(FORMATTER) : "");
                qMap.put("source", q.getSource() != null ? q.getSource() : "");
                latestQuestions.add(qMap);
            }
            stat.put("latestQuestions", latestQuestions);
            
            categoryStats.add(stat);
        }
        
        result.put("categoryStats", categoryStats);
        result.put("totalCount", categoryStats.stream().mapToLong(s -> (Long) s.get("count")).sum());
        
        return result;
    }
}
