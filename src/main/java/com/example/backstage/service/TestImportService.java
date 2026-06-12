package com.example.backstage.service;

import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.entity.QuestionTest;
import com.example.backstage.repository.QuestionTestRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 测试导入服务 - 将解析的题目导入到 test 表
 */
@Service
public class TestImportService {

    private static final Logger logger = LoggerFactory.getLogger(TestImportService.class);
    
    private final QuestionTestRepository questionTestRepository;
    private final ObjectMapper objectMapper;

    public TestImportService(QuestionTestRepository questionTestRepository) {
        this.questionTestRepository = questionTestRepository;
        this.objectMapper = new ObjectMapper();
    }

    /**
     * 将解析的题目列表导入到 test 表（兼容旧版本，无source参数）
     */
    @Transactional
    public Map<String, Object> importQuestionsToTest(List<ParsedQuestionDTO> questions) {
        return importQuestionsToTest(questions, null);
    }
    
    /**
     * 将解析的题目列表导入到 test 表
     * @param questions 题目列表
     * @param source 来源名称
     */
    @Transactional
    public Map<String, Object> importQuestionsToTest(List<ParsedQuestionDTO> questions, String source) {
        Map<String, Object> result = new HashMap<>();
        List<QuestionTest> savedQuestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        
        // 使用传入的source或默认值
        String finalSource = (source != null && !source.isEmpty()) ? source : "批量导入";
        
        try {
            for (int i = 0; i < questions.size(); i++) {
                ParsedQuestionDTO dto = questions.get(i);
                try {
                    QuestionTest questionTest = convertToQuestionTest(dto, i + 1, finalSource);
                    QuestionTest saved = questionTestRepository.save(questionTest);
                    savedQuestions.add(saved);
                    logger.info("成功保存题目到 test 表: id={}, title={}, source={}", saved.getId(), dto.getTitle(), finalSource);
                } catch (Exception e) {
                    String error = String.format("题目 %d 保存失败: %s", i + 1, e.getMessage());
                    errors.add(error);
                    logger.error(error, e);
                }
            }
            
            result.put("success", true);
            result.put("total", questions.size());
            result.put("saved", savedQuestions.size());
            result.put("errors", errors);
            result.put("message", String.format("成功导入 %d 道题目到 test 表", savedQuestions.size()));
            
        } catch (Exception e) {
            logger.error("批量导入失败", e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
        }
        
        return result;
    }

    /**
     * 将 ParsedQuestionDTO 转换为 QuestionTest 实体（兼容旧版本）
     */
    private QuestionTest convertToQuestionTest(ParsedQuestionDTO dto, int orderNum) {
        return convertToQuestionTest(dto, orderNum, "批量导入");
    }
    
    /**
     * 将 ParsedQuestionDTO 转换为 QuestionTest 实体
     */
    private QuestionTest convertToQuestionTest(ParsedQuestionDTO dto, int orderNum, String source) {
        QuestionTest questionTest = new QuestionTest();
        
        // 设置基本信息
        questionTest.setQuestionId(generateQuestionId(orderNum));
        questionTest.setTitle(dto.getTitle());
        
        // 设置分类信息 - 确保category_id是英文，category_name是中文
        String categoryId = getCategoryId(dto.getCategory());
        String categoryName = getCategoryName(categoryId);
        questionTest.setCategoryId(categoryId);
        questionTest.setCategoryName(categoryName);
        
        // 设置难度
        questionTest.setDifficulty(dto.getDifficulty() != null ? dto.getDifficulty() : "medium");
        
        // 设置题目内容（JSON格式）
        questionTest.setQuestionContent(buildQuestionContentJson(dto));
        
        // 设置答案和解析
        questionTest.setCorrectAnswer(dto.getCorrectAnswer());
        questionTest.setAnswerAnalysis(dto.getAnalysis());
        
        // 设置状态
        questionTest.setStatus("active");
        
        // 设置其他字段
        questionTest.setHasImage(false);
        questionTest.setHasMaterial(false);
        questionTest.setSource(source); // 使用传入的来源名称
        
        return questionTest;
    }

    /**
     * 生成题目ID
     */
    private String generateQuestionId(int orderNum) {
        return "Q" + System.currentTimeMillis() + String.format("%04d", orderNum);
    }

    /**
     * 分类映射：英文ID -> 中文名称
     */
    private static final Map<String, String> CATEGORY_ID_TO_NAME = new HashMap<>();
    static {
        CATEGORY_ID_TO_NAME.put("political_theory", "政治理论");
        CATEGORY_ID_TO_NAME.put("quantity_relation", "数量关系");
        CATEGORY_ID_TO_NAME.put("material_analysis", "资料分析");
        CATEGORY_ID_TO_NAME.put("common_sense_judgment", "常识判断");
        CATEGORY_ID_TO_NAME.put("logical_judgment", "判断推理");
        CATEGORY_ID_TO_NAME.put("language_understanding", "言语理解");
    }
    
    /**
     * 分类映射：中文名称 -> 英文ID
     */
    private static final Map<String, String> CATEGORY_NAME_TO_ID = new HashMap<>();
    static {
        CATEGORY_NAME_TO_ID.put("政治理论", "political_theory");
        CATEGORY_NAME_TO_ID.put("数量关系", "quantity_relation");
        CATEGORY_NAME_TO_ID.put("资料分析", "material_analysis");
        CATEGORY_NAME_TO_ID.put("常识判断", "common_sense_judgment");
        CATEGORY_NAME_TO_ID.put("判断推理", "logical_judgment");
        CATEGORY_NAME_TO_ID.put("言语理解", "language_understanding");
        CATEGORY_NAME_TO_ID.put("言语理解与表达", "language_understanding");
    }
    
    /**
     * 获取分类名称（根据英文ID返回中文名称）
     */
    private String getCategoryName(String categoryId) {
        if (categoryId == null) return "未分类";
        
        // 如果已经是中文名称，直接返回
        if (CATEGORY_NAME_TO_ID.containsKey(categoryId)) {
            return categoryId;
        }
        
        return CATEGORY_ID_TO_NAME.getOrDefault(categoryId, "未分类");
    }
    
    /**
     * 获取分类ID（根据中文名称返回英文ID）
     */
    private String getCategoryId(String categoryName) {
        if (categoryName == null) return "other";
        
        // 如果已经是英文ID，直接返回
        if (CATEGORY_ID_TO_NAME.containsKey(categoryName)) {
            return categoryName;
        }
        
        return CATEGORY_NAME_TO_ID.getOrDefault(categoryName, "other");
    }

    /**
     * 构建题目内容的JSON字符串（按照指定格式）
     */
    private String buildQuestionContentJson(ParsedQuestionDTO dto) {
        try {
            Map<String, Object> content = new HashMap<>();
            content.put("image", ""); // 图片字段为空
            
            // 将选项数组转换为 {"A": "xxx", "B": "xxx", ...} 格式
            Map<String, String> optionsMap = new HashMap<>();
            if (dto.getOptions() != null) {
                for (String option : dto.getOptions()) {
                    // 提取选项字母和内容
                    String trimmed = option.trim();
                    if (trimmed.length() >= 2) {
                        String key = trimmed.substring(0, 1).toUpperCase(); // 获取A/B/C/D
                        String value = trimmed.substring(1).trim();
                        // 移除可能的点号或顿号
                        if (value.startsWith(".") || value.startsWith("、")) {
                            value = value.substring(1).trim();
                        }
                        optionsMap.put(key, value);
                    }
                }
            }
            content.put("option", optionsMap);
            
            return objectMapper.writeValueAsString(content);
        } catch (Exception e) {
            logger.error("构建题目内容JSON失败", e);
            return "{}";
        }
    }

    /**
     * 清空 test 表
     */
    @Transactional
    public void clearTestTable() {
        questionTestRepository.deleteAll();
        logger.info("已清空 test 表");
    }

    /**
     * 获取 test 表中的题目数量
     */
    public long getTestTableCount() {
        return questionTestRepository.count();
    }
}