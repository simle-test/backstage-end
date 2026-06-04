package com.example.backstage.service.impl;

import com.example.backstage.dto.response.MaterialAnalysisDetailResponse;
import com.example.backstage.dto.response.MaterialProblemItem;
import com.example.backstage.dto.request.SaveMaterialAnalysisRequest;
import com.example.backstage.dto.request.MaterialProblemRequest;
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

import java.util.*;
import java.util.stream.Collectors;

@Service
public class MaterialAnalysisServiceImpl implements MaterialAnalysisService {

    private final MaterialRepository materialRepository;
    private final QuestionRepository questionRepository;
    private final MaterialProblemRepository materialProblemRepository;

    private static final Map<String, String> DIFFICULTY_MAP = new HashMap<>();
    static {
        DIFFICULTY_MAP.put("easy", "简单");
        DIFFICULTY_MAP.put("medium", "中等");
        DIFFICULTY_MAP.put("hard", "困难");
    }

    public MaterialAnalysisServiceImpl(MaterialRepository materialRepository,
                                       QuestionRepository questionRepository,
                                       MaterialProblemRepository materialProblemRepository) {
        this.materialRepository = materialRepository;
        this.questionRepository = questionRepository;
        this.materialProblemRepository = materialProblemRepository;
    }

    @Override
    public MaterialAnalysisDetailResponse getMaterialAnalysisDetail(Integer materialId) {
        // 从questions_total表获取所有关联的题目（包括大题干和小题）
        List<Question> questions = questionRepository.findByMaterialId(materialId);

        // 从material_problems表获取小题列表
        List<MaterialProblem> problems = materialProblemRepository.findByMaterialIdOrderByIdAsc(materialId);

        // 转换为响应DTO
        MaterialAnalysisDetailResponse response = new MaterialAnalysisDetailResponse();
        response.setMaterialId(materialId);

        if (!questions.isEmpty()) {
            // 第一个题目作为大题干
            Question mainQuestion = questions.get(0);
            
            // 设置图片URL
            response.setImageUrl(mainQuestion.getImageUrl());

            // 设置来源和年份
            response.setSource(mainQuestion.getSource());
            response.setYear(mainQuestion.getYear());

            // 从questions_total表获取主题干内容，按优先级从多个字段尝试
            String materialContent = mainQuestion.getContentText();
            if (materialContent == null || materialContent.isEmpty()) {
                // 尝试从title字段获取
                if (mainQuestion.getTitle() != null && !mainQuestion.getTitle().isEmpty()) {
                    materialContent = mainQuestion.getTitle();
                }
                // 尝试从answerAnalysis字段获取
                if ((materialContent == null || materialContent.isEmpty()) && mainQuestion.getAnswerAnalysis() != null && !mainQuestion.getAnswerAnalysis().isEmpty()) {
                    materialContent = mainQuestion.getAnswerAnalysis();
                }
                // 尝试从tips字段获取
                if ((materialContent == null || materialContent.isEmpty()) && mainQuestion.getTips() != null && !mainQuestion.getTips().isEmpty()) {
                    materialContent = mainQuestion.getTips();
                }
                // 尝试从questionContent字段获取（JSON字段，提取文本内容）
                if ((materialContent == null || materialContent.isEmpty()) && mainQuestion.getQuestionContent() != null) {
                    String qc = mainQuestion.getQuestionContent();
                    // 简单提取JSON中的文本内容
                    if (qc.length() > 0) {
                        materialContent = qc.replaceAll("[{}\"\\[\\]:,]", " ").trim();
                        // 如果提取后还是太长，截取前500个字符
                        if (materialContent.length() > 500) {
                            materialContent = materialContent.substring(0, 500) + "...";
                        }
                    }
                }
            }
            response.setMaterialContent(materialContent);

            // 如果questions_total表中有多个题目（除了大题干外还有小题），直接使用这些小题
            List<MaterialProblemItem> problemItems;
            if (questions.size() > 1) {
                // 跳过第一个（大题干），从第二个开始作为小题
                problemItems = questions.stream()
                        .skip(1)
                        .map(this::convertToProblemItem)
                        .collect(Collectors.toList());
            } else if (!problems.isEmpty()) {
                // 否则使用material_problems表中的小题
                problemItems = problems.stream()
                        .map(problem -> convertProblemToItemWithDetails(problem))
                        .collect(Collectors.toList());
            } else {
                problemItems = java.util.Collections.emptyList();
            }
            response.setProblems(problemItems);
            response.setProblemCount(problemItems.size());
        }

        // 从material表获取材料信息
        Material material = materialRepository.findByMaterialId(materialId);
        if (material != null) {
            response.setMaterialTitle(material.getTitle());
            response.setCategoryId(material.getCategoryId());
            response.setCategoryName(material.getCategoryName());
            // 如果之前没有获取到内容，尝试从material表获取
            if ((response.getMaterialContent() == null || response.getMaterialContent().isEmpty()) && material.getContent() != null) {
                response.setMaterialContent(material.getContent());
            }
        }

        return response;
    }

    private MaterialProblemItem convertToProblemItem(Question question) {
        MaterialProblemItem item = new MaterialProblemItem();
        item.setId(question.getId());
        item.setQuestionId(question.getQuestionId());
        item.setQuestionText(question.getTitle());
        item.setCorrectAnswer(question.getCorrectAnswer());
        item.setDifficulty(question.getDifficulty() != null ? question.getDifficulty() : "medium");
        item.setDifficultyText(DIFFICULTY_MAP.getOrDefault(item.getDifficulty(), item.getDifficulty()));
        
        // 从JSON字段解析选项
        String questionContent = question.getQuestionContent();
        if (questionContent != null && !questionContent.isEmpty()) {
            // 简单解析JSON获取选项
            item.setOptionA(parseOption(questionContent, "A"));
            item.setOptionB(parseOption(questionContent, "B"));
            item.setOptionC(parseOption(questionContent, "C"));
            item.setOptionD(parseOption(questionContent, "D"));
            item.setOptionE(parseOption(questionContent, "E"));
        }
        
        return item;
    }
    
    private String parseOption(String jsonContent, String optionKey) {
        try {
            // 查找 "option" 对象
            String optionKey_str = "\"option\"";
            int optionIndex = jsonContent.indexOf(optionKey_str);
            if (optionIndex == -1) {
                // 尝试直接查找选项key
                return parseDirectOption(jsonContent, optionKey);
            }
            
            // 找到 option 对象的开始位置
            int braceStart = jsonContent.indexOf("{", optionIndex);
            if (braceStart == -1) {
                return "";
            }
            
            // 找到 option 对象的结束位置（匹配的大括号）
            int braceEnd = findMatchingBrace(jsonContent, braceStart);
            if (braceEnd == -1) {
                return "";
            }
            
            String optionObj = jsonContent.substring(braceStart + 1, braceEnd);
            
            // 在 option 对象中查找指定 key
            String key = "\"" + optionKey + "\"";
            int keyIndex = optionObj.indexOf(key);
            if (keyIndex == -1) {
                return "";
            }
            
            // 找到冒号后的值
            int colonIndex = optionObj.indexOf(":", keyIndex);
            if (colonIndex == -1) {
                return "";
            }
            
            int valueStart = colonIndex + 1;
            // 跳过空格
            while (valueStart < optionObj.length() && 
                   (optionObj.charAt(valueStart) == ' ' || optionObj.charAt(valueStart) == '"')) {
                valueStart++;
            }
            
            // 找到值的结束位置
            int valueEnd = valueStart;
            boolean inQuotes = false;
            while (valueEnd < optionObj.length()) {
                char c = optionObj.charAt(valueEnd);
                if (c == '"' && !inQuotes) {
                    inQuotes = true;
                    valueEnd++;
                    continue;
                } else if (c == '"' && inQuotes) {
                    // 检查是否是转义的引号
                    if (valueEnd > 0 && optionObj.charAt(valueEnd - 1) == '\\') {
                        valueEnd++;
                        continue;
                    }
                    inQuotes = false;
                    valueEnd++;
                    break;
                }
                if (!inQuotes && (c == ',' || c == '}')) {
                    break;
                }
                valueEnd++;
            }
            
            String result = optionObj.substring(valueStart, valueEnd).trim();
            // 去除末尾的引号、逗号、空格
            while (result.endsWith("\"") || result.endsWith(",") || result.endsWith(" ")) {
                result = result.substring(0, result.length() - 1).trim();
            }
            return result;
        } catch (Exception e) {
            return "";
        }
    }
    
    private String parseDirectOption(String jsonContent, String optionKey) {
        try {
            String key = "\"" + optionKey + "\"";
            int keyIndex = jsonContent.indexOf(key);
            if (keyIndex == -1) {
                return "";
            }
            
            int colonIndex = jsonContent.indexOf(":", keyIndex);
            if (colonIndex == -1) {
                return "";
            }
            
            int valueStart = colonIndex + 1;
            while (valueStart < jsonContent.length() && 
                   (jsonContent.charAt(valueStart) == ' ' || jsonContent.charAt(valueStart) == '"')) {
                valueStart++;
            }
            
            int valueEnd = valueStart;
            boolean inQuotes = false;
            while (valueEnd < jsonContent.length()) {
                char c = jsonContent.charAt(valueEnd);
                if (c == '"' && !inQuotes) {
                    inQuotes = true;
                    valueEnd++;
                    continue;
                } else if (c == '"' && inQuotes) {
                    if (valueEnd > 0 && jsonContent.charAt(valueEnd - 1) == '\\') {
                        valueEnd++;
                        continue;
                    }
                    inQuotes = false;
                    valueEnd++;
                    break;
                }
                if (!inQuotes && (c == ',' || c == '}')) {
                    break;
                }
                valueEnd++;
            }
            
            String result = jsonContent.substring(valueStart, valueEnd).trim();
            // 去除末尾的引号、逗号、空格
            while (result.endsWith("\"") || result.endsWith(",") || result.endsWith(" ")) {
                result = result.substring(0, result.length() - 1).trim();
            }
            return result;
        } catch (Exception e) {
            return "";
        }
    }
    
    private int findMatchingBrace(String str, int openBraceIndex) {
        int count = 1;
        int i = openBraceIndex + 1;
        boolean inQuotes = false;
        while (i < str.length() && count > 0) {
            char c = str.charAt(i);
            if (c == '"' && (i == 0 || str.charAt(i - 1) != '\\')) {
                inQuotes = !inQuotes;
            } else if (!inQuotes) {
                if (c == '{') {
                    count++;
                } else if (c == '}') {
                    count--;
                }
            }
            if (count == 0) {
                return i;
            }
            i++;
        }
        return -1;
    }

    /**
     * 将 MaterialProblem 实体转换为 MaterialProblemItem，直接从 material_problems 表获取所有信息
     */
    private MaterialProblemItem convertProblemToItemWithDetails(MaterialProblem problem) {
        MaterialProblemItem item = new MaterialProblemItem();
        item.setId(problem.getId());
        item.setQuestionId("");
        item.setQuestionText(problem.getTitle()); // 从material_problems获取题目内容
        item.setOrderNum(problem.getId()); // 使用id作为排序号
        
        // 从 material_problems 表的 question_content 字段解析选项
        String questionContent = problem.getQuestionContent();
        if (questionContent != null && !questionContent.isEmpty()) {
            item.setOptionA(parseOption(questionContent, "A"));
            item.setOptionB(parseOption(questionContent, "B"));
            item.setOptionC(parseOption(questionContent, "C"));
            item.setOptionD(parseOption(questionContent, "D"));
            item.setOptionE(parseOption(questionContent, "E"));
        } else {
            item.setOptionA("");
            item.setOptionB("");
            item.setOptionC("");
            item.setOptionD("");
            item.setOptionE("");
        }
        
        // 设置默认值
        item.setCorrectAnswer(problem.getCorrectAnswer() != null ? problem.getCorrectAnswer() : "");
        item.setAnalysis(problem.getAnswerAnalysis() != null ? problem.getAnswerAnalysis() : "");
        item.setDifficulty("medium");
        item.setDifficultyText("中等");
        
        return item;
    }

    /**
     * 将 MaterialProblem 实体转换为 MaterialProblemItem（简单版本）
     */
    private MaterialProblemItem convertProblemToItem(MaterialProblem problem) {
        MaterialProblemItem item = new MaterialProblemItem();
        item.setId(problem.getId());
        item.setQuestionId("");
        item.setQuestionText(problem.getTitle()); // 使用title字段作为题目内容
        item.setOptionA("");
        item.setOptionB("");
        item.setOptionC("");
        item.setOptionD("");
        item.setOptionE("");
        item.setCorrectAnswer("");
        item.setOrderNum(problem.getId()); // 使用id作为排序号
        item.setDifficulty("medium");
        item.setDifficultyText("中等");
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
        Question mainQuestion;
        List<Question> existingQuestions = questionRepository.findByMaterialId(material.getMaterialId());
        if (!existingQuestions.isEmpty()) {
            mainQuestion = existingQuestions.get(0);
        } else {
            mainQuestion = new Question();
            mainQuestion.setMaterialId(material.getMaterialId());
            mainQuestion.setHasMaterial(true);
        }
        mainQuestion.setContentText(request.getMaterialContent());
        mainQuestion.setSource(request.getSource());
        mainQuestion.setYear(request.getYear());
        questionRepository.save(mainQuestion);

        // 保存小题列表
        if (request.getProblems() != null) {
            // 获取现有小题
            List<MaterialProblem> existingProblems = materialProblemRepository.findByMaterialIdOrderByIdAsc(material.getMaterialId());
            Map<Integer, MaterialProblem> existingProblemMap = existingProblems.stream()
                    .collect(Collectors.toMap(MaterialProblem::getId, p -> p));

            // 处理提交的小题
            Set<Integer> processedIds = new HashSet<>();
            for (MaterialProblemRequest problemItem : request.getProblems()) {
                MaterialProblem problem;
                if (problemItem.getId() != null && existingProblemMap.containsKey(problemItem.getId())) {
                    problem = existingProblemMap.get(problemItem.getId());
                } else {
                    problem = new MaterialProblem();
                    problem.setMaterialId(material.getMaterialId());
                }
                problem.setTitle(problemItem.getQuestionText());
                materialProblemRepository.save(problem);
                processedIds.add(problem.getId());
            }

            // 删除未包含在请求中的现有小题
            for (MaterialProblem existingProblem : existingProblems) {
                if (!processedIds.contains(existingProblem.getId())) {
                    materialProblemRepository.delete(existingProblem);
                }
            }
        }
    }
}
