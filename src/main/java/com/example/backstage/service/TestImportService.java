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
import java.util.*;
import java.util.stream.Collectors;

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
        return importQuestionsToTestWithDedup(questions, source, true, "exact", 0.9, List.of("title"));
    }
    
    /**
     * 将解析的题目列表导入到 test 表（带去重参数）
     * @param questions 题目列表
     * @param source 来源名称
     * @param enableDeduplication 是否启用去重
     * @param dedupMode 去重模式：exact（精确匹配）或 similar（模糊匹配）
     * @param similarityThreshold 相似度阈值（模糊匹配时使用）
     * @param dedupFields 去重字段
     */
    @Transactional
    public Map<String, Object> importQuestionsToTestWithDedup(
            List<ParsedQuestionDTO> questions, 
            String source,
            Boolean enableDeduplication,
            String dedupMode,
            Double similarityThreshold,
            List<String> dedupFields) {
        
        Map<String, Object> result = new HashMap<>();
        List<QuestionTest> savedQuestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        List<Map<String, Object>> duplicatedQuestions = new ArrayList<>();
        int skippedCount = 0;
        
        // 使用传入的source或默认值
        String finalSource = (source != null && !source.isEmpty()) ? source : "批量导入";
        
        try {
            // 获取数据库中已存在的题目（用于去重）
            Set<String> existingTitles = new HashSet<>();
            if (enableDeduplication != null && enableDeduplication) {
                List<QuestionTest> existingQuestions = questionTestRepository.findAll();
                for (QuestionTest existing : existingQuestions) {
                    if (existing.getTitle() != null) {
                        existingTitles.add(normalizeForDeduplication(existing.getTitle()));
                    }
                }
                logger.info("数据库中已存在 {} 道题目，将进行去重检查", existingTitles.size());
            }
            
            // 用于本次导入内的去重
            Set<String> importedTitles = new HashSet<>();
            
            for (int i = 0; i < questions.size(); i++) {
                ParsedQuestionDTO dto = questions.get(i);
                try {
                    String normalizedTitle = normalizeForDeduplication(dto.getTitle());
                    
                    // 检查是否与数据库中已存在的题目重复
                    boolean isDuplicate = false;
                    if (enableDeduplication != null && enableDeduplication && normalizedTitle != null) {
                        isDuplicate = existingTitles.contains(normalizedTitle);
                    }
                    
                    // 检查本次导入内是否重复
                    if (!isDuplicate && normalizedTitle != null && importedTitles.contains(normalizedTitle)) {
                        isDuplicate = true;
                    }
                    
                    if (isDuplicate) {
                        skippedCount++;
                        Map<String, Object> dupInfo = new HashMap<>();
                        dupInfo.put("title", truncateForLog(dto.getTitle()));
                        dupInfo.put("index", i + 1);
                        duplicatedQuestions.add(dupInfo);
                        logger.info("跳过重复题目: title={}", truncateForLog(dto.getTitle()));
                        continue;
                    }
                    
                    QuestionTest questionTest = convertToQuestionTest(dto, i + 1, finalSource);
                    QuestionTest saved = questionTestRepository.save(questionTest);
                    savedQuestions.add(saved);
                    
                    // 添加到已导入集合
                    if (normalizedTitle != null) {
                        importedTitles.add(normalizedTitle);
                    }
                    
                    logger.info("成功保存题目到 test 表: id={}, title={}, source={}", saved.getId(), truncateForLog(dto.getTitle()), finalSource);
                } catch (Exception e) {
                    String error = String.format("题目 %d 保存失败: %s", i + 1, e.getMessage());
                    errors.add(error);
                    logger.error(error, e);
                }
            }
            
            result.put("success", true);
            result.put("total", questions.size());
            result.put("saved", savedQuestions.size());
            result.put("skipped", skippedCount);
            result.put("duplicates", duplicatedQuestions);
            result.put("errors", errors);
            
            StringBuilder message = new StringBuilder();
            message.append(String.format("成功导入 %d 道题目", savedQuestions.size()));
            if (skippedCount > 0) {
                message.append(String.format("，跳过 %d 道重复题目", skippedCount));
            }
            result.put("message", message.toString());
            
            logger.info("导入完成: total={}, saved={}, skipped={}", questions.size(), savedQuestions.size(), skippedCount);
            
        } catch (Exception e) {
            logger.error("批量导入失败", e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 将解析的题目列表导入到 test 表（覆盖重复模式）
     * @param questions 题目列表
     * @param source 来源名称
     */
    @Transactional
    public Map<String, Object> importQuestionsToTestWithOverride(List<ParsedQuestionDTO> questions, String source) {
        Map<String, Object> result = new HashMap<>();
        List<QuestionTest> savedQuestions = new ArrayList<>();
        List<String> errors = new ArrayList<>();
        int overrideCount = 0;
        
        String finalSource = (source != null && !source.isEmpty()) ? source : "批量导入";
        
        try {
            // 获取数据库中已存在的题目（用于查找重复）
            Map<String, QuestionTest> existingByTitle = new HashMap<>();
            List<QuestionTest> existingQuestions = questionTestRepository.findAll();
            for (QuestionTest existing : existingQuestions) {
                if (existing.getTitle() != null) {
                    String normalizedTitle = normalizeForDeduplication(existing.getTitle());
                    if (normalizedTitle != null) {
                        existingByTitle.put(normalizedTitle, existing);
                    }
                }
            }
            logger.info("数据库中已存在 {} 道题目，将进行覆盖检查", existingByTitle.size());
            
            for (int i = 0; i < questions.size(); i++) {
                ParsedQuestionDTO dto = questions.get(i);
                try {
                    String normalizedTitle = normalizeForDeduplication(dto.getTitle());
                    QuestionTest existingQuestion = normalizedTitle != null ? existingByTitle.get(normalizedTitle) : null;
                    
                    QuestionTest questionTest;
                    if (existingQuestion != null) {
                        // 覆盖现有题目
                        questionTest = convertToQuestionTest(dto, i + 1, finalSource);
                        questionTest.setId(existingQuestion.getId());
                        questionTest.setCreatedAt(existingQuestion.getCreatedAt());
                        overrideCount++;
                        logger.info("覆盖题目: id={}, title={}", existingQuestion.getId(), truncateForLog(dto.getTitle()));
                    } else {
                        // 新增题目
                        questionTest = convertToQuestionTest(dto, i + 1, finalSource);
                    }
                    
                    QuestionTest saved = questionTestRepository.save(questionTest);
                    savedQuestions.add(saved);
                    
                    logger.info("成功保存题目: id={}, title={}, source={}", saved.getId(), truncateForLog(dto.getTitle()), finalSource);
                } catch (Exception e) {
                    String error = String.format("题目 %d 保存失败: %s", i + 1, e.getMessage());
                    errors.add(error);
                    logger.error(error, e);
                }
            }
            
            result.put("success", true);
            result.put("total", questions.size());
            result.put("saved", savedQuestions.size());
            result.put("override", overrideCount);
            result.put("errors", errors);
            
            StringBuilder message = new StringBuilder();
            message.append(String.format("成功导入 %d 道题目", savedQuestions.size()));
            if (overrideCount > 0) {
                message.append(String.format("，覆盖 %d 道重复题目", overrideCount));
            }
            result.put("message", message.toString());
            
            logger.info("导入完成: total={}, saved={}, override={}", questions.size(), savedQuestions.size(), overrideCount);
            
        } catch (Exception e) {
            logger.error("批量导入失败", e);
            result.put("success", false);
            result.put("message", "导入失败: " + e.getMessage());
        }
        
        return result;
    }
    
    /**
     * 标准化题目用于去重比较
     */
    private String normalizeForDeduplication(String text) {
        if (text == null || text.trim().isEmpty()) {
            return null;
        }
        // 去除多余空白、转小写
        String result = text.trim().toLowerCase().replaceAll("\\s+", "");
        // 去除标点符号 - 使用字符码范围
        result = result.replaceAll("[,./;:'\"!?()\\[\\]{}]", "");
        // 去除常见中文标点（单个替换）
        String chinesePunctuation = "，。、；：！？「」『』（）【】《》";
        for (char c : chinesePunctuation.toCharArray()) {
            result = result.replace(String.valueOf(c), "");
        }
        return result;
    }
    
    /**
     * 截断日志输出
     */
    private String truncateForLog(String text) {
        if (text == null) return "null";
        return text.length() > 50 ? text.substring(0, 50) + "..." : text;
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