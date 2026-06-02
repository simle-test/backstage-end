package com.example.backstage.service.impl;

import com.example.backstage.dto.request.CreateQuestionRequest;
import com.example.backstage.dto.request.UpdateQuestionRequest;
import com.example.backstage.dto.response.*;
import com.example.backstage.entity.Material;
import com.example.backstage.entity.Question;
import com.example.backstage.repository.MaterialRepository;
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
    private final MaterialRepository materialRepository;

    public QuestionServiceImpl(QuestionRepository questionRepository, MaterialRepository materialRepository) {
        this.questionRepository = questionRepository;
        this.materialRepository = materialRepository;
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

        Page<Question> questionPage;
        if (keyword == null) keyword = "";
        if (difficulty == null) difficulty = "";
        if (category == null) category = "";

        // 如果查询的是材料分析题，返回有material_id的题目或者返回所有题目
        if ("material_analysis".equals(category)) {
            // 先尝试查询有material_id的题目
            List<Question> materialQuestions = questionRepository.findByMaterialIdIsNotNull();
            if (!materialQuestions.isEmpty()) {
                // 如果有材料分析题，只返回这些题目
                List<QuestionListItem> list = materialQuestions.stream()
                    .map(this::convertToListItem)
                    .collect(Collectors.toList());
                return new QuestionListResponse(list, (long) list.size(), page, size);
            } else {
                // 如果没有材料分析题，返回所有题目作为临时方案
                questionPage = questionRepository.findByDifficultyContainingAndTitleContaining(difficulty, keyword, pageable);
            }
        } else {
            questionPage = questionRepository.findByCategoryIdContainingAndDifficultyContainingAndTitleContaining(
                category, difficulty, keyword, pageable);
        }

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

        return new QuestionStatisticsResponse(total, easyCount, mediumCount, hardCount, 0.0);
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

    private QuestionListItem convertToListItem(Question question) {
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

        // 如果是资料分析题，查询材料信息
        if (question.getMaterialId() != null && "material_analysis".equals(question.getCategoryId())) {
            Material material = materialRepository.findByMaterialId(question.getMaterialId());
            if (material != null) {
                response.setMaterialId(material.getMaterialId());
                response.setMaterialTitle(material.getTitle());
                response.setMaterialContent(material.getContent());
                // 材料图片 URL 可以从材料的 content 中解析，或者根据 materialId 构建
                response.setMaterialImageUrl("/api/materials/" + material.getMaterialId() + "/image");
            }
        }

        return response;
    }
}
