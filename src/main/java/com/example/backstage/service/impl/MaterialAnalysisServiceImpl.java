package com.example.backstage.service.impl;

import com.example.backstage.dto.request.MaterialProblemRequest;
import com.example.backstage.dto.request.SaveMaterialAnalysisRequest;
import com.example.backstage.dto.response.MaterialAnalysisDetailResponse;
import com.example.backstage.dto.response.MaterialProblemItem;
import com.example.backstage.entity.Material;
import com.example.backstage.entity.MaterialProblem;
import com.example.backstage.entity.Question;
import com.example.backstage.repository.MaterialProblemRepository;
import com.example.backstage.repository.MaterialRepository;
import com.example.backstage.repository.QuestionRepository;
import com.example.backstage.service.MaterialAnalysisService;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 资料分析题服务实现
 */
@Service
public class MaterialAnalysisServiceImpl implements MaterialAnalysisService {

    private final MaterialRepository materialRepository;
    private final MaterialProblemRepository materialProblemRepository;
    private final QuestionRepository questionRepository;

    private static final Map<String, String> DIFFICULTY_MAP = new HashMap<>();
    static {
        DIFFICULTY_MAP.put("easy", "简单");
        DIFFICULTY_MAP.put("medium", "中等");
        DIFFICULTY_MAP.put("hard", "困难");
    }

    public MaterialAnalysisServiceImpl(MaterialRepository materialRepository,
                                       MaterialProblemRepository materialProblemRepository,
                                       QuestionRepository questionRepository) {
        this.materialRepository = materialRepository;
        this.materialProblemRepository = materialProblemRepository;
        this.questionRepository = questionRepository;
    }

    @Override
    public MaterialAnalysisDetailResponse getMaterialAnalysisDetail(Integer materialId) {
        // 查询关联的主表题目（获取content_text主题干、source和year）
        List<Question> mainQuestions = questionRepository.findByMaterialId(materialId);
        Question mainQuestion = mainQuestions.isEmpty() ? null : mainQuestions.get(0);

        if (mainQuestion == null) {
            return null;
        }

        // 从主表题目的content_text字段获取主题干内容
        Material material = materialRepository.findByMaterialId(materialId);

        // 查询所有小题
        List<MaterialProblem> problems = materialProblemRepository.findByMaterialIdOrderByOrderNumAsc(materialId);

        // 转换为响应DTO
        MaterialAnalysisDetailResponse response = new MaterialAnalysisDetailResponse();
        response.setMaterialId(materialId);

        // 设置材料标题（如果有）
        if (material != null) {
            response.setMaterialTitle(material.getTitle());
            response.setCategoryId(material.getCategoryId());
            response.setCategoryName(material.getCategoryName());
        }

        // 重要：从questions_total.content_text获取主题干内容
        response.setMaterialContent(mainQuestion.getContentText());

        // 设置来源和年份
        response.setSource(mainQuestion.getSource());
        response.setYear(mainQuestion.getYear());

        // 转换小题列表
        List<MaterialProblemItem> problemItems = problems.stream()
                .map(this::convertToProblemItem)
                .collect(Collectors.toList());
        response.setProblems(problemItems);

        return response;
    }

    private MaterialProblemItem convertToProblemItem(MaterialProblem problem) {
        MaterialProblemItem item = new MaterialProblemItem();
        item.setId(problem.getId());
        item.setQuestionId(problem.getQuestionId());
        item.setQuestionText(problem.getQuestionText());
        item.setOptionA(problem.getOptionA());
        item.setOptionB(problem.getOptionB());
        item.setOptionC(problem.getOptionC());
        item.setOptionD(problem.getOptionD());
        item.setOptionE(problem.getOptionE());
        item.setCorrectAnswer(problem.getCorrectAnswer());
        item.setAnalysis(problem.getAnalysis());
        item.setOrderNum(problem.getOrderNum());
        item.setDifficulty(problem.getDifficulty());
        item.setDifficultyText(DIFFICULTY_MAP.getOrDefault(problem.getDifficulty(), problem.getDifficulty()));
        return item;
    }

    @Override
    @Transactional
    public void saveMaterialAnalysis(SaveMaterialAnalysisRequest request) {
        // 保存材料信息
        Material material;
        if (request.getMaterialId() != null) {
            material = materialRepository.findByMaterialId(request.getMaterialId());
            if (material == null) {
                throw new EntityNotFoundException("材料不存在");
            }
        } else {
            material = new Material();
            material.setCategoryId("material_analysis");
            material.setCategoryName("资料分析");
        }

        material.setTitle(request.getMaterialTitle());
        material.setContent(request.getMaterialContent());
        material = materialRepository.save(material);

        // 保存或更新主表题目（用于存储content_text主题干、source和year）
        Question mainQuestion = questionRepository.findByMaterialId(material.getMaterialId()).stream()
                .findFirst()
                .orElse(null);

        if (mainQuestion == null) {
            mainQuestion = new Question();
            mainQuestion.setMaterialId(material.getMaterialId());
            mainQuestion.setCategoryId("material_analysis");
            mainQuestion.setCategoryName("资料分析");
            mainQuestion.setContentText(request.getMaterialContent());
        }

        mainQuestion.setSource(request.getSource());
        mainQuestion.setYear(request.getYear());
        mainQuestion.setContentText(request.getMaterialContent());
        questionRepository.save(mainQuestion);

        // 删除旧的小题，然后保存新的小题
        materialProblemRepository.deleteByMaterialId(material.getMaterialId());

        if (request.getProblems() != null) {
            for (MaterialProblemRequest problemRequest : request.getProblems()) {
                MaterialProblem problem = new MaterialProblem();
                problem.setMaterialId(material.getMaterialId());
                problem.setQuestionId(problemRequest.getQuestionId());
                problem.setQuestionText(problemRequest.getQuestionText());
                problem.setOptionA(problemRequest.getOptionA());
                problem.setOptionB(problemRequest.getOptionB());
                problem.setOptionC(problemRequest.getOptionC());
                problem.setOptionD(problemRequest.getOptionD());
                problem.setOptionE(problemRequest.getOptionE());
                problem.setCorrectAnswer(problemRequest.getCorrectAnswer());
                problem.setAnalysis(problemRequest.getAnalysis());
                problem.setOrderNum(problemRequest.getOrderNum());
                problem.setDifficulty(problemRequest.getDifficulty() != null ? problemRequest.getDifficulty() : "medium");
                problem.setStatus("active");
                materialProblemRepository.save(problem);
            }
        }
    }
}
