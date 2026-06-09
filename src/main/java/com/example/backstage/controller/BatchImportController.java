package com.example.backstage.controller;

import com.example.backstage.dto.MaterialQuestionDTO;
import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.service.AiParseService;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * 批量导入控制器 - 支持题目和答案分离上传
 */
@RestController
@RequestMapping("/questions")
public class BatchImportController {

    private static final Logger logger = LoggerFactory.getLogger(BatchImportController.class);
    
    private final AiParseService aiParseService;

    public BatchImportController(AiParseService aiParseService) {
        this.aiParseService = aiParseService;
    }

    // 题目分类映射
    private static final Map<String, String> CATEGORY_MAP = new LinkedHashMap<>();
    static {
        CATEGORY_MAP.put("political_theory", "政治理论");
        CATEGORY_MAP.put("quantity_relation", "数量关系");
        CATEGORY_MAP.put("material_analysis", "资料分析");
        CATEGORY_MAP.put("common_sense_judgment", "常识判断");
        CATEGORY_MAP.put("logical_judgment", "判断推理");
        CATEGORY_MAP.put("language_understanding", "言语理解");
    }

    // 题型识别模式 - 更宽松的模式
    private static final Pattern QUESTION_START_PATTERN = Pattern.compile(
        "^\\s*(\\d+)[.、,，\\s]+(.+)"
    );

    // 选项识别模式
    private static final Pattern OPTION_PATTERN = Pattern.compile(
        "^\\s*([A-E])\\s*[.、:：]\\s*(.+)"
    );
    
    // 题型关键词映射
    private static final Map<String, String> QUESTION_TYPE_KEYWORDS = new HashMap<>();
    static {
        QUESTION_TYPE_KEYWORDS.put("单选", "single_choice");
        QUESTION_TYPE_KEYWORDS.put("单项", "single_choice");
        QUESTION_TYPE_KEYWORDS.put("选择", "single_choice");
        QUESTION_TYPE_KEYWORDS.put("多选", "multiple_choice");
        QUESTION_TYPE_KEYWORDS.put("判断", "true_false");
        QUESTION_TYPE_KEYWORDS.put("填空", "fill_blank");
        QUESTION_TYPE_KEYWORDS.put("简答", "essay");
        QUESTION_TYPE_KEYWORDS.put("论述", "essay");
        QUESTION_TYPE_KEYWORDS.put("案例分析", "essay");
    }

    /**
     * AI 智能解析预览 - 使用 DeepSeek AI 进行智能题目识别
     */
    @PostMapping("/batch-import/preview-ai")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewImportWithAI(
            @RequestParam("questionFile") MultipartFile questionFile,
            @RequestParam(value = "answerFile", required = false) MultipartFile answerFile,
            @RequestParam(value = "category", required = false) String category) {
        
        logger.info("收到AI批量导入预览请求: questionFile={}, answerFile={}, category={}",
                    questionFile.getOriginalFilename(),
                    answerFile != null ? answerFile.getOriginalFilename() : "无",
                    category);
        
        try {
            // 1. 读取文档内容
            String content = readDocumentContent(questionFile);
            
            if (content.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("文档内容为空"));
            }
            
            // 2. 使用AI解析
            List<ParsedQuestionDTO> parsedQuestions = aiParseService.parseWithAI(content);
            
            if (parsedQuestions.isEmpty()) {
                // AI解析失败，回退到规则引擎
                logger.warn("AI解析失败，回退到规则引擎");
                List<String> lines = parseDocument(questionFile);
                parsedQuestions = parseQuestions(lines);
                
                if (parsedQuestions.isEmpty()) {
                    return ResponseEntity.ok(ApiResponse.error("未能识别到有效题目，请检查文档格式"));
                }
            }
            
            // 3. 解析答案文件（如果提供）
            if (answerFile != null && !answerFile.isEmpty()) {
                Map<String, AnswerAnalysis> answers = parseAnswerFile(answerFile);
                matchAnswers(parsedQuestions, answers);
            }
            
            // 4. 如果指定了分类，覆盖识别的分类
            if (category != null && !category.isEmpty()) {
                for (ParsedQuestionDTO question : parsedQuestions) {
                    question.setCategory(category);
                }
            }
            
            // 5. 统计各类型数量
            Map<String, Integer> stats = new HashMap<>();
            stats.put("total", parsedQuestions.size());
            stats.put("singleChoice", 0);
            stats.put("multipleChoice", 0);
            stats.put("fillBlank", 0);
            stats.put("trueFalse", 0);
            stats.put("essay", 0);
            
            for (ParsedQuestionDTO question : parsedQuestions) {
                String type = question.getType();
                switch (type) {
                    case "single_choice":
                        stats.put("singleChoice", stats.get("singleChoice") + 1);
                        break;
                    case "multiple_choice":
                        stats.put("multipleChoice", stats.get("multipleChoice") + 1);
                        break;
                    case "fill_blank":
                        stats.put("fillBlank", stats.get("fillBlank") + 1);
                        break;
                    case "true_false":
                        stats.put("trueFalse", stats.get("trueFalse") + 1);
                        break;
                    case "essay":
                        stats.put("essay", stats.get("essay") + 1);
                        break;
                }
            }
            
            // 6. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("questions", parsedQuestions);
            result.put("statistics", stats);
            result.put("message", "成功识别 " + parsedQuestions.size() + " 道题目（AI解析）");
            result.put("parseMethod", "ai");
            
            logger.info("AI解析成功: total={}", parsedQuestions.size());
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            logger.error("AI解析文档失败", e);
            return ResponseEntity.ok(ApiResponse.error("解析失败: " + e.getMessage()));
        }
    }

    /**
     * 预览导入 - 解析文档并识别题目（规则引擎，支持资料分析大题）
     */
    @PostMapping("/batch-import/preview")
    public ResponseEntity<ApiResponse<Map<String, Object>>> previewImport(
            @RequestParam("questionFile") MultipartFile questionFile,
            @RequestParam(value = "answerFile", required = false) MultipartFile answerFile,
            @RequestParam(value = "category", required = false) String category) {
        
        logger.info("收到批量导入预览请求: questionFile={}, answerFile={}, category={}",
                    questionFile.getOriginalFilename(),
                    answerFile != null ? answerFile.getOriginalFilename() : "无",
                    category);
        
        try {
            // 1. 解析题目文件
            List<String> questionLines = parseDocument(questionFile);
            
            if (questionLines.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("文档内容为空"));
            }
            
            // 2. 识别题目（支持资料分析大题分组）
            Map<String, Object> parseResult = parseQuestionsWithMaterial(questionLines);
            List<ParsedQuestionDTO> normalQuestions = (List<ParsedQuestionDTO>) parseResult.get("normalQuestions");
            List<MaterialQuestionDTO> materialQuestions = (List<MaterialQuestionDTO>) parseResult.get("materialQuestions");
            
            // 合并所有题目用于统计
            List<ParsedQuestionDTO> allQuestions = new ArrayList<>(normalQuestions);
            for (MaterialQuestionDTO mq : materialQuestions) {
                allQuestions.addAll(mq.getSubQuestions());
            }
            
            if (allQuestions.isEmpty()) {
                return ResponseEntity.ok(ApiResponse.error("未能识别到有效题目，请检查文档格式"));
            }
            
            // 3. 解析答案文件（如果提供）
            if (answerFile != null && !answerFile.isEmpty()) {
                Map<String, AnswerAnalysis> answers = parseAnswerFile(answerFile);
                // 为普通题目匹配答案
                matchAnswers(normalQuestions, answers);
                // 为材料大题的子题目匹配答案
                for (MaterialQuestionDTO mq : materialQuestions) {
                    matchAnswers(mq.getSubQuestions(), answers);
                }
            }
            
            // 4. 如果指定了分类，覆盖识别的分类
            if (category != null && !category.isEmpty()) {
                for (ParsedQuestionDTO question : normalQuestions) {
                    question.setCategory(category);
                }
                for (MaterialQuestionDTO mq : materialQuestions) {
                    mq.setCategory(category);
                    for (ParsedQuestionDTO sq : mq.getSubQuestions()) {
                        sq.setCategory(category);
                    }
                }
            }
            
            // 5. 统计各类型数量
            Map<String, Integer> stats = new HashMap<>();
            stats.put("total", allQuestions.size());
            stats.put("singleChoice", 0);
            stats.put("multipleChoice", 0);
            stats.put("fillBlank", 0);
            stats.put("trueFalse", 0);
            stats.put("essay", 0);
            stats.put("materialAnalysis", materialQuestions.size());
            
            for (ParsedQuestionDTO question : allQuestions) {
                String type = question.getType();
                switch (type) {
                    case "single_choice":
                        stats.put("singleChoice", stats.get("singleChoice") + 1);
                        break;
                    case "multiple_choice":
                        stats.put("multipleChoice", stats.get("multipleChoice") + 1);
                        break;
                    case "fill_blank":
                        stats.put("fillBlank", stats.get("fillBlank") + 1);
                        break;
                    case "true_false":
                        stats.put("trueFalse", stats.get("trueFalse") + 1);
                        break;
                    case "essay":
                        stats.put("essay", stats.get("essay") + 1);
                        break;
                }
            }
            
            // 6. 构建响应
            Map<String, Object> result = new HashMap<>();
            result.put("questions", normalQuestions);
            result.put("materialQuestions", materialQuestions);
            result.put("statistics", stats);
            result.put("message", "成功识别 " + allQuestions.size() + " 道题目（包含 " + materialQuestions.size() + " 道资料分析大题）");
            
            logger.info("成功解析题目: total={}, 普通题目={}, 材料大题={}", 
                        allQuestions.size(), normalQuestions.size(), materialQuestions.size());
            
            return ResponseEntity.ok(ApiResponse.success(result));
            
        } catch (Exception e) {
            logger.error("解析文档失败", e);
            return ResponseEntity.ok(ApiResponse.error("解析失败: " + e.getMessage()));
        }
    }

    /**
     * 获取支持的题目分类
     */
    @GetMapping("/batch-import/categories")
    public ResponseEntity<ApiResponse<Map<String, String>>> getCategories() {
        return ResponseEntity.ok(ApiResponse.success(CATEGORY_MAP));
    }

    /**
     * 读取文档内容为字符串（用于AI解析）
     */
    private String readDocumentContent(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("文件名不能为空");
        }

        StringBuilder content = new StringBuilder();
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".docx")) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    String text = paragraph.getText().trim();
                    if (!text.isEmpty()) {
                        content.append(text).append("\n");
                    }
                }
            }
        } else if (lowerFilename.endsWith(".txt")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        content.append(trimmedLine).append("\n");
                    }
                }
            }
        } else if (lowerFilename.endsWith(".doc")) {
            throw new IOException("暂不支持 .doc 格式，请将文档另存为 .docx 格式后重试");
        } else {
            throw new IOException("不支持的文件格式，请上传 .docx 或 .txt 文件");
        }
        
        return content.toString().trim();
    }

    /**
     * 解析文档文件
     */
    private List<String> parseDocument(MultipartFile file) throws IOException {
        String filename = file.getOriginalFilename();
        if (filename == null) {
            throw new IOException("文件名不能为空");
        }

        List<String> lines = new ArrayList<>();
        String lowerFilename = filename.toLowerCase();
        
        if (lowerFilename.endsWith(".docx")) {
            try (XWPFDocument document = new XWPFDocument(file.getInputStream())) {
                for (XWPFParagraph paragraph : document.getParagraphs()) {
                    String text = paragraph.getText().trim();
                    if (!text.isEmpty()) {
                        lines.add(text);
                    }
                }
            }
        } else if (lowerFilename.endsWith(".txt")) {
            try (BufferedReader reader = new BufferedReader(
                    new InputStreamReader(file.getInputStream(), StandardCharsets.UTF_8))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    String trimmedLine = line.trim();
                    if (!trimmedLine.isEmpty()) {
                        lines.add(trimmedLine);
                    }
                }
            }
        } else if (lowerFilename.endsWith(".doc")) {
            throw new IOException("暂不支持 .doc 格式，请将文档另存为 .docx 格式后重试");
        } else {
            throw new IOException("不支持的文件格式，请上传 .docx 或 .txt 文件");
        }
        
        return lines;
    }

    /**
     * 解析题目列表（支持资料分析大题分组）
     */
    private Map<String, Object> parseQuestionsWithMaterial(List<String> lines) {
        Map<String, Object> result = new HashMap<>();
        List<ParsedQuestionDTO> normalQuestions = new ArrayList<>(); // 普通题目
        List<MaterialQuestionDTO> materialQuestions = new ArrayList<>(); // 资料分析大题
        
        StringBuilder currentMaterial = new StringBuilder();
        List<ParsedQuestionDTO> currentSubQuestions = new ArrayList<>();
        ParsedQuestionDTO currentQuestion = null;
        List<String> currentOptions = new ArrayList<>();
        int orderNum = 1;
        int materialOrderNum = 1;
        String currentCategory = null;
        boolean inMaterialAnalysis = false;
        boolean inMaterialBlock = false; // 是否在材料块中（收集材料内容）
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            // 检查是否是分类标题
            if (line.matches("^第[一二三四五六七八九十]+(部分|编|章)\\s*.+")) {
                Matcher catMatcher = Pattern.compile("^第[一二三四五六七八九十]+(部分|编|章)\\s*(.+)").matcher(line);
                if (catMatcher.find()) {
                    currentCategory = catMatcher.group(2).trim();
                    logger.debug("识别到分类: {}", currentCategory);
                    if (currentCategory.contains("资料分析")) {
                        inMaterialAnalysis = true;
                    } else {
                        inMaterialAnalysis = false;
                    }
                }
                // 如果之前有未完成的材料大题，先保存
                if (inMaterialBlock && currentMaterial.length() > 0) {
                    saveMaterialQuestion(materialQuestions, currentMaterial, currentSubQuestions, currentQuestion, currentOptions, currentCategory, materialOrderNum++);
                    currentMaterial = new StringBuilder();
                    currentSubQuestions = new ArrayList<>();
                    currentQuestion = null;
                    currentOptions.clear();
                }
                continue;
            }
            
            // 检查是否是材料开头（如"材料一："、"材料三：根据以下资料"）
            if (inMaterialAnalysis && line.matches("^材料[一二三四五六七八九十\\d]+[：:]?.*")) {
                // 如果之前有未完成的材料大题，先保存
                if (inMaterialBlock && currentMaterial.length() > 0) {
                    saveMaterialQuestion(materialQuestions, currentMaterial, currentSubQuestions, currentQuestion, currentOptions, currentCategory, materialOrderNum++);
                }
                // 开始新的材料块
                inMaterialBlock = true;
                currentMaterial = new StringBuilder();
                currentSubQuestions = new ArrayList<>();
                currentMaterial.append(line).append("\n");
                continue;
            }
            
            // 如果在材料块中
            if (inMaterialBlock) {
                // 如果是题目行，处理题目
                if (isQuestionLine(line)) {
                    currentQuestion = processQuestionLine(line, currentQuestion, currentOptions, currentSubQuestions, currentCategory);
                }
                // 如果当前有题目，检查是否是选项
                else if (currentQuestion != null) {
                    Matcher optionMatcher = OPTION_PATTERN.matcher(line);
                    if (optionMatcher.find()) {
                        currentOptions.add(optionMatcher.group(1) + ". " + optionMatcher.group(2).trim());
                    } else {
                        // 不是选项也不是题目，可能是题目内容的延续
                        currentQuestion.setTitle(currentQuestion.getTitle() + " " + line);
                    }
                }
                // 否则添加到材料内容
                else {
                    currentMaterial.append(line).append("\n");
                }
                continue;
            }
            
            // 非资料分析的普通题目
            if (!inMaterialAnalysis) {
                // 如果是题目行
                if (isQuestionLine(line)) {
                    currentQuestion = processQuestionLine(line, currentQuestion, currentOptions, normalQuestions, currentCategory);
                }
                // 如果当前有题目，检查是否是选项
                else if (currentQuestion != null) {
                    Matcher optionMatcher = OPTION_PATTERN.matcher(line);
                    if (optionMatcher.find()) {
                        currentOptions.add(optionMatcher.group(1) + ". " + optionMatcher.group(2).trim());
                    } else {
                        // 不是选项也不是题目，可能是题目内容的延续
                        currentQuestion.setTitle(currentQuestion.getTitle() + " " + line);
                    }
                }
            }
        }
        
        // 保存最后一个材料大题
        if (inMaterialBlock && currentMaterial.length() > 0) {
            saveMaterialQuestion(materialQuestions, currentMaterial, currentSubQuestions, currentQuestion, currentOptions, currentCategory, materialOrderNum);
            // 重置以避免重复添加到普通题目
            currentQuestion = null;
            currentOptions.clear();
        }
        
        // 保存最后一个普通题目（非资料分析的题目）
        if (currentQuestion != null && !inMaterialBlock) {
            currentQuestion.setOptions(new ArrayList<>(currentOptions));
            normalQuestions.add(currentQuestion);
        }
        
        result.put("normalQuestions", normalQuestions);
        result.put("materialQuestions", materialQuestions);
        
        return result;
    }
    
    /**
     * 判断是否是题目行
     */
    private boolean isQuestionLine(String line) {
        // 匹配数字开头的题目（如 "121. 题目内容（）"）
        Matcher matcher = QUESTION_START_PATTERN.matcher(line);
        if (matcher.find()) {
            String questionText = matcher.group(2).trim();
            // 包含"（）"或"()"表示选择题，或者题号大于100（资料分析题通常从100+开始）
            try {
                int num = Integer.parseInt(matcher.group(1));
                return num >= 100 || questionText.contains("（）") || questionText.contains("()");
            } catch (NumberFormatException e) {
                return questionText.contains("（）") || questionText.contains("()");
            }
        }
        return false;
    }
    
    /**
     * 处理题目行（修改为返回当前题目）
     */
    private ParsedQuestionDTO processQuestionLine(String line, ParsedQuestionDTO currentQuestion, List<String> currentOptions, 
                                     List<ParsedQuestionDTO> questions, String category) {
        Matcher matcher = QUESTION_START_PATTERN.matcher(line);
        if (matcher.find()) {
            // 保存之前的题目
            if (currentQuestion != null) {
                currentQuestion.setOptions(new ArrayList<>(currentOptions));
                questions.add(currentQuestion);
                currentOptions.clear();
            }
            
            // 创建新题目
            currentQuestion = new ParsedQuestionDTO();
            currentQuestion.setOrderNum(questions.size() + 1);
            currentQuestion.setType(detectQuestionType(matcher.group(2).trim()));
            currentQuestion.setDifficulty("medium");
            currentQuestion.setTitle(matcher.group(2).trim());
            currentQuestion.setCategory(category);
        } else {
            // 检查是否是选项
            Matcher optionMatcher = OPTION_PATTERN.matcher(line);
            if (optionMatcher.find() && currentQuestion != null) {
                currentOptions.add(optionMatcher.group(1) + ". " + optionMatcher.group(2).trim());
            }
        }
        return currentQuestion;
    }
    
    /**
     * 保存材料大题
     */
    private void saveMaterialQuestion(List<MaterialQuestionDTO> materialQuestions, 
                                      StringBuilder currentMaterial, 
                                      List<ParsedQuestionDTO> currentSubQuestions,
                                      ParsedQuestionDTO currentQuestion,
                                      List<String> currentOptions,
                                      String category, int orderNum) {
        if (currentMaterial.length() > 0) {
            // 保存最后一个未保存的题目
            if (currentQuestion != null && currentOptions.size() > 0) {
                currentQuestion.setOptions(new ArrayList<>(currentOptions));
                currentSubQuestions.add(currentQuestion);
            }
            
            MaterialQuestionDTO materialQuestion = new MaterialQuestionDTO();
            materialQuestion.setMaterial(currentMaterial.toString().trim());
            materialQuestion.setSubQuestions(currentSubQuestions);
            materialQuestion.setCategory(category);
            materialQuestion.setOrderNum(orderNum);
            materialQuestions.add(materialQuestion);
            logger.debug("创建资料分析大题: 材料长度={}, 子题目数={}", 
                        currentMaterial.length(), currentSubQuestions.size());
        }
    }

    /**
     * 解析题目列表
     */
    private List<ParsedQuestionDTO> parseQuestions(List<String> lines) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();
        ParsedQuestionDTO currentQuestion = null;
        List<String> currentOptions = new ArrayList<>();
        int orderNum = 1;
        String currentCategory = null;
        boolean inMaterialAnalysis = false;
        boolean foundFirstQuestion = false;
        
        for (int i = 0; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            
            // 检查是否是分类标题（如"第一部分 常识判断"）
            if (line.matches("^第[一二三四五六七八九十]+(部分|编|章)\\s*.+")) {
                Matcher catMatcher = Pattern.compile("^第[一二三四五六七八九十]+(部分|编|章)\\s*(.+)").matcher(line);
                if (catMatcher.find()) {
                    currentCategory = catMatcher.group(2).trim();
                    logger.debug("识别到分类: {}", currentCategory);
                    // 如果是资料分析，设置标记
                    if (currentCategory.contains("资料分析")) {
                        inMaterialAnalysis = true;
                    }
                }
                continue;
            }
            
            // 检查是否是新题目的开始（数字开头）
            Matcher matcher = QUESTION_START_PATTERN.matcher(line);
            if (matcher.find()) {
                String questionNumber = matcher.group(1);
                String questionText = matcher.group(2).trim();
                
                // 对于资料分析题，需要更严格地判断
                if (inMaterialAnalysis && !foundFirstQuestion) {
                    try {
                        int num = Integer.parseInt(questionNumber);
                        // 资料分析题的题号通常从100+开始，或者题目必须包含"（）"表示选择题
                        boolean isQuestionNumber = num >= 100 || questionText.contains("（）") || questionText.contains("()");
                        
                        if (!isQuestionNumber) {
                            logger.debug("跳过材料数据行: {}", line);
                            continue;
                        }
                    } catch (NumberFormatException e) {
                        // 不是数字，跳过
                        logger.debug("跳过非数字开头的材料行: {}", line);
                        continue;
                    }
                    foundFirstQuestion = true;
                }
                
                // 保存之前的题目
                if (currentQuestion != null) {
                    currentQuestion.setOptions(new ArrayList<>(currentOptions));
                    questions.add(currentQuestion);
                    logger.debug("识别题目 {}: {} - {}", currentQuestion.getOrderNum(), 
                        currentQuestion.getType(), currentQuestion.getTitle().substring(0, Math.min(20, currentQuestion.getTitle().length())));
                }
                
                // 创建新题目
                currentQuestion = new ParsedQuestionDTO();
                currentQuestion.setOrderNum(orderNum++);
                currentQuestion.setType(detectQuestionType(questionText));
                currentQuestion.setDifficulty("medium");
                currentQuestion.setTitle(questionText);
                if (currentCategory != null) {
                    currentQuestion.setCategory(currentCategory);
                }
                currentOptions.clear();
            } 
            // 检查是否是选项
            else if (currentQuestion != null) {
                Matcher optionMatcher = OPTION_PATTERN.matcher(line);
                if (optionMatcher.find()) {
                    currentOptions.add(optionMatcher.group(1) + ". " + optionMatcher.group(2).trim());
                }
                // 检查是否是另一道题目（没有选项直接到下一题）
                else if (line.matches("^\\d+[.、,，]\\s*.+")) {
                    // 保存当前题目
                    currentQuestion.setOptions(new ArrayList<>(currentOptions));
                    questions.add(currentQuestion);
                    logger.debug("识别题目 {}: {} - {}", currentQuestion.getOrderNum(), 
                        currentQuestion.getType(), currentQuestion.getTitle().substring(0, Math.min(20, currentQuestion.getTitle().length())));
                    
                    // 创建新题目
                    Matcher newMatcher = QUESTION_START_PATTERN.matcher(line);
                    if (newMatcher.find()) {
                        currentQuestion = new ParsedQuestionDTO();
                        currentQuestion.setOrderNum(orderNum++);
                        currentQuestion.setType(detectQuestionType(newMatcher.group(2).trim()));
                        currentQuestion.setDifficulty("medium");
                        currentQuestion.setTitle(newMatcher.group(2).trim());
                        if (currentCategory != null) {
                            currentQuestion.setCategory(currentCategory);
                        }
                    }
                    currentOptions.clear();
                }
                // 多行题目内容
                else if (currentOptions.isEmpty()) {
                    String existingTitle = currentQuestion.getTitle();
                    if (existingTitle != null && !existingTitle.isEmpty()) {
                        currentQuestion.setTitle(existingTitle + " " + line);
                    }
                }
            }
        }
        
        // 保存最后一个题目
        if (currentQuestion != null) {
            currentQuestion.setOptions(new ArrayList<>(currentOptions));
            questions.add(currentQuestion);
            logger.debug("识别题目 {}: {} - {}", currentQuestion.getOrderNum(), 
                currentQuestion.getType(), currentQuestion.getTitle().substring(0, Math.min(20, currentQuestion.getTitle().length())));
        }
        
        logger.info("共识别 {} 道题目", questions.size());
        return questions;
    }
    
    /**
     * 判断是否可能是材料内容（用于资料分析题）
     */
    private boolean isLikelyMaterialContent(String text) {
        // 如果包含以下关键词，可能是材料数据
        String[] materialKeywords = {"同比增长", "进出口", "总额", "产值", "利润率", "贸易顺差", "同比下降", "亿美元", "占比"};
        for (String keyword : materialKeywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * 从题目文本中识别题型
     */
    private String detectQuestionType(String text) {
        for (Map.Entry<String, String> entry : QUESTION_TYPE_KEYWORDS.entrySet()) {
            if (text.contains(entry.getKey())) {
                return entry.getValue();
            }
        }
        // 默认根据选项数量判断
        return "single_choice";
    }

    /**
     * 解析答案文件（包含答案和解析）
     */
    private Map<String, AnswerAnalysis> parseAnswerFile(MultipartFile file) throws IOException {
        Map<String, AnswerAnalysis> answerMap = new LinkedHashMap<>();
        List<String> lines = parseDocument(file);
        
        logger.info("开始解析答案文件: {}, 共 {} 行", file.getOriginalFilename(), lines.size());
        
        // 打印前20行内容
        int count = 0;
        for (String line : lines) {
            if (count++ < 20) {
                logger.info("答案文件第 {} 行: '{}'", count, line);
            } else {
                break;
            }
        }
        
        String currentNum = null;
        StringBuilder currentAnalysis = new StringBuilder();
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 匹配答案格式（新题开始）
            // 格式1: 1. A
            // 格式2: 答案1：A
            // 格式3: 答案1：A 解析内容...
            // 格式4: 1A
            // 格式5: 第1题：A
            // 格式6: 1、A
            if (line.matches("^\\d+[.、,，:：]\\s*[A-Ea-e]+.*")) {
                // 先保存上一题的解析
                if (currentNum != null && currentAnalysis.length() > 0) {
                    AnswerAnalysis aa = answerMap.get(currentNum);
                    if (aa != null) {
                        aa.setAnalysis(currentAnalysis.toString().trim());
                    }
                    currentAnalysis = new StringBuilder();
                }
                
                // 使用正则提取题号和答案部分
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^(\\d+)[.、,，:：]\\s*([A-Ea-e]+)\\s*(.*)$");
                java.util.regex.Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String num = matcher.group(1).trim();
                    String answer = matcher.group(2).toUpperCase();
                    String analysis = matcher.group(3).trim();
                    
                    AnswerAnalysis aa = new AnswerAnalysis();
                    aa.setAnswer(answer);
                    answerMap.put(num, aa);
                    currentNum = num;
                    
                    // 如果有解析内容，直接保存
                    if (!analysis.isEmpty()) {
                        currentAnalysis.append(analysis).append(" ");
                    }
                }
            } else if (line.matches("^[答案]+[0-9]+[：:]\\s*[A-Ea-e]+.*")) {
                // 先保存上一题的解析
                if (currentNum != null && currentAnalysis.length() > 0) {
                    AnswerAnalysis aa = answerMap.get(currentNum);
                    if (aa != null) {
                        aa.setAnalysis(currentAnalysis.toString().trim());
                    }
                    currentAnalysis = new StringBuilder();
                }
                
                String num = line.replaceAll("[^0-9]", "").trim();
                String rest = line.replaceAll("^[答案]+[0-9]+[：:]", "").trim();
                String answer = rest.replaceAll("[^A-Ea-e]", "").toUpperCase();
                if (!answer.isEmpty()) {
                    AnswerAnalysis aa = new AnswerAnalysis();
                    aa.setAnswer(answer);
                    answerMap.put(num, aa);
                    currentNum = num;
                    
                    // 检查是否有解析内容
                    String analysis = rest.replaceAll("^[A-Ea-e]\\s*", "").trim();
                    if (!analysis.isEmpty()) {
                        currentAnalysis.append(analysis).append(" ");
                    }
                }
            } else if (line.matches("^第\\d+题[：:]?\\s*[A-Ea-e]+.*")) {
                // 格式: 第1题：A 解析内容
                if (currentNum != null && currentAnalysis.length() > 0) {
                    AnswerAnalysis aa = answerMap.get(currentNum);
                    if (aa != null) {
                        aa.setAnalysis(currentAnalysis.toString().trim());
                    }
                    currentAnalysis = new StringBuilder();
                }
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("^第(\\d+)题[：:]?\\s*([A-Ea-e]+)\\s*(.*)$");
                java.util.regex.Matcher matcher = pattern.matcher(line);
                if (matcher.find()) {
                    String num = matcher.group(1).trim();
                    String answer = matcher.group(2).toUpperCase();
                    String analysis = matcher.group(3).trim();
                    
                    AnswerAnalysis aa = new AnswerAnalysis();
                    aa.setAnswer(answer);
                    answerMap.put(num, aa);
                    currentNum = num;
                    
                    if (!analysis.isEmpty()) {
                        currentAnalysis.append(analysis).append(" ");
                    }
                }
            } else if (line.matches("^[0-9]+[A-Ea-e]+")) {
                // 格式: 1A
                if (currentNum != null && currentAnalysis.length() > 0) {
                    AnswerAnalysis aa = answerMap.get(currentNum);
                    if (aa != null) {
                        aa.setAnalysis(currentAnalysis.toString().trim());
                    }
                    currentAnalysis = new StringBuilder();
                }
                
                String num = line.replaceAll("[^0-9]", "");
                String answer = line.replaceAll("[^A-Ea-e]", "").toUpperCase();
                if (!answer.isEmpty()) {
                    AnswerAnalysis aa = new AnswerAnalysis();
                    aa.setAnswer(answer);
                    answerMap.put(num, aa);
                    currentNum = num;
                }
            } else if (currentNum != null) {
                // 继续解析内容
                currentAnalysis.append(line).append(" ");
            }
        }
        
        // 保存最后一题的解析
        if (currentNum != null && currentAnalysis.length() > 0) {
            AnswerAnalysis aa = answerMap.get(currentNum);
            if (aa != null) {
                aa.setAnalysis(currentAnalysis.toString().trim());
            }
        }
        
        logger.info("解析答案文件完成，共匹配 {} 道题的答案", answerMap.size());
        return answerMap;
    }

    /**
     * 为题目匹配答案和解析
     */
    private void matchAnswers(List<ParsedQuestionDTO> questions, Map<String, AnswerAnalysis> answerMap) {
        int matchedCount = 0;
        int unmatchedCount = 0;
        
        logger.info("开始匹配答案，共有 {} 道题目，{} 条答案记录", questions.size(), answerMap.size());
        
        // 打印所有可用的答案键
        if (!answerMap.isEmpty()) {
            logger.info("可用的答案题号: {}", String.join(", ", answerMap.keySet()));
        }
        
        for (ParsedQuestionDTO question : questions) {
            String num = String.valueOf(question.getOrderNum());
            if (answerMap.containsKey(num)) {
                AnswerAnalysis aa = answerMap.get(num);
                question.setCorrectAnswer(aa.getAnswer());
                question.setAnalysis(aa.getAnalysis());
                logger.debug("题目 {} 匹配到答案: {}, 解析: {}", num, aa.getAnswer(), 
                    aa.getAnalysis() != null && aa.getAnalysis().length() > 50 ? 
                        aa.getAnalysis().substring(0, 50) + "..." : aa.getAnalysis());
                matchedCount++;
            } else {
                logger.debug("题目 {} 未匹配到答案", num);
                unmatchedCount++;
            }
        }
        
        logger.info("答案匹配完成: 匹配 {} 道，未匹配 {} 道", matchedCount, unmatchedCount);
    }

    /**
     * 答案和解析数据结构
     */
    private static class AnswerAnalysis {
        private String answer;
        private String analysis;
        
        public String getAnswer() {
            return answer;
        }
        
        public void setAnswer(String answer) {
            this.answer = answer;
        }
        
        public String getAnalysis() {
            return analysis;
        }
        
        public void setAnalysis(String analysis) {
            this.analysis = analysis;
        }
    }

    /**
     * 从行中提取分类
     */
    private String extractCategoryFromLine(String line) {
        Map<String, String> keywords = new HashMap<>();
        keywords.put("political_theory", "政治");
        keywords.put("quantity_relation", "数量|数学|计算");
        keywords.put("material_analysis", "资料分析|图表");
        keywords.put("common_sense_judgment", "常识|历史|地理|科技");
        keywords.put("logical_judgment", "逻辑|推理|判断");
        keywords.put("language_understanding", "言语|理解|阅读|选词");
        
        for (Map.Entry<String, String> entry : keywords.entrySet()) {
            if (Pattern.compile(entry.getValue()).matcher(line).find()) {
                return entry.getKey();
            }
        }
        return null;
    }
}
