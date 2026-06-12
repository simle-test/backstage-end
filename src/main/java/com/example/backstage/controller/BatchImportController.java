package com.example.backstage.controller;

import com.example.backstage.dto.ImportRequestDTO;
import com.example.backstage.dto.MaterialQuestionDTO;
import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.dto.response.ApiResponse;
import com.example.backstage.service.AiParseService;
import com.example.backstage.service.TestImportService;
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
    private final TestImportService testImportService;

    public BatchImportController(AiParseService aiParseService, TestImportService testImportService) {
        this.aiParseService = aiParseService;
        this.testImportService = testImportService;
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
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "source", required = false) String source) {
        
        logger.info("收到AI批量导入预览请求: questionFile={}, answerFile={}, category={}, source={}",
                    questionFile.getOriginalFilename(),
                    answerFile != null ? answerFile.getOriginalFilename() : "无",
                    category, source);
        
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
            result.put("source", source); // 添加来源名称
            
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
            @RequestParam(value = "category", required = false) String category,
            @RequestParam(value = "source", required = false) String source) {
        
        logger.info("收到批量导入预览请求: questionFile={}, answerFile={}, category={}, source={}",
                    questionFile.getOriginalFilename(),
                    answerFile != null ? answerFile.getOriginalFilename() : "无",
                    category, source);
        
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
            result.put("source", source); // 添加来源名称
            
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
            
            // 检查是否是分类标题（支持多种格式）
            String categoryMatch = extractCategory(line);
            if (categoryMatch != null) {
                // 保存之前可能未完成的题目
                if (currentQuestion != null && currentOptions.size() > 0) {
                    currentQuestion.setOptions(new ArrayList<>(currentOptions));
                    if (inMaterialBlock) {
                        currentSubQuestions.add(currentQuestion);
                    } else if (!inMaterialAnalysis) {
                        normalQuestions.add(currentQuestion);
                    }
                    currentQuestion = null;
                    currentOptions.clear();
                }
                
                currentCategory = categoryMatch;
                logger.info("识别到分类: {}", currentCategory);
                
                // 判断是否是资料分析
                inMaterialAnalysis = currentCategory.contains("资料分析");
                
                // 如果之前有未完成的材料大题，先保存
                if (inMaterialBlock && currentMaterial.length() > 0) {
                    saveMaterialQuestion(materialQuestions, currentMaterial, currentSubQuestions, currentQuestion, currentOptions, currentCategory, materialOrderNum++);
                    currentMaterial = new StringBuilder();
                    currentSubQuestions = new ArrayList<>();
                    inMaterialBlock = false;
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
     * 格式说明：
     * - 题号. 答案（如 "21. A"）
     * - 解析从【解析】开始，到"故正确答案为"结束
     */
    private Map<String, AnswerAnalysis> parseAnswerFile(MultipartFile file) throws IOException {
        Map<String, AnswerAnalysis> answerMap = new LinkedHashMap<>();
        List<String> lines = parseDocument(file);
        
        logger.info("========== 开始解析答案文件 ==========");
        logger.info("文件名: {}, 共 {} 行", file.getOriginalFilename(), lines.size());
        
        // 打印所有行内容，方便调试
        int lineNum = 0;
        for (String line : lines) {
            lineNum++;
            logger.info("答案文件第 {} 行: '{}'", lineNum, line);
        }
        
        String currentNum = null;
        StringBuilder currentAnalysis = new StringBuilder();
        boolean inAnalysis = false; // 是否正在解析【解析】部分
        
        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;
            
            // 检测【解析】标记，开始收集解析内容
            if (line.contains("【解析】")) {
                inAnalysis = true;
                // 提取【解析】后面的内容
                String analysisStart = line.substring(line.indexOf("【解析】") + 4).trim();
                if (!analysisStart.isEmpty()) {
                    currentAnalysis.append(analysisStart).append(" ");
                }
                logger.debug("进入解析模式，当前题目: {}, 解析开始内容: {}", currentNum, analysisStart);
                continue;
            }
            
            // 如果正在收集解析内容
            if (inAnalysis) {
                // 检查是否遇到"故正确答案为"，这是解析结束的标记
                if (line.contains("故正确答案为")) {
                    // 保存当前解析内容（不包含"故正确答案为"部分）
                    int endIndex = line.indexOf("故正确答案为");
                    if (endIndex > 0) {
                        String beforeAnswer = line.substring(0, endIndex).trim();
                        if (!beforeAnswer.isEmpty()) {
                            currentAnalysis.append(beforeAnswer).append(" ");
                        }
                    }
                    
                    // 保存解析到当前题目
                    if (currentNum != null && currentAnalysis.length() > 0) {
                        AnswerAnalysis aa = answerMap.get(currentNum);
                        if (aa != null) {
                            aa.setAnalysis(currentAnalysis.toString().trim());
                            logger.info("题目 {} 的解析已保存，长度: {}", currentNum, aa.getAnalysis().length());
                        }
                    }
                    
                    // 尝试从"故正确答案为"后面提取答案
                    String answerPart = line.substring(line.indexOf("故正确答案为") + 5).trim();
                    String answer = extractAnswerFromLine(answerPart);
                    if (!answer.isEmpty() && currentNum != null) {
                        AnswerAnalysis aa = answerMap.get(currentNum);
                        if (aa != null && (aa.getAnswer() == null || aa.getAnswer().isEmpty())) {
                            aa.setAnswer(answer);
                            logger.info("题目 {} 的答案已更新为: {}", currentNum, answer);
                        }
                    }
                    
                    // 重置状态
                    currentAnalysis.setLength(0);
                    inAnalysis = false;
                    currentNum = null;
                    continue;
                }
                
                // 继续收集解析内容
                currentAnalysis.append(line).append(" ");
                logger.debug("追加解析内容: {}", line);
                continue;
            }
            
            // 匹配题号.答案格式（如 "21. A"）
            java.util.regex.Pattern numAnswerPattern = java.util.regex.Pattern.compile("^(\\d+)[.．、,，]\\s*([A-Ea-e]+)\\s*(.*)$");
            java.util.regex.Matcher numAnswerMatcher = numAnswerPattern.matcher(line);
            
            if (numAnswerMatcher.find()) {
                String num = numAnswerMatcher.group(1).trim();
                String answer = numAnswerMatcher.group(2).toUpperCase();
                
                AnswerAnalysis aa = new AnswerAnalysis();
                aa.setAnswer(answer);
                answerMap.put(num, aa);
                currentNum = num;
                logger.info("匹配题目 {}，答案: {}", num, answer);
            }
            // 处理章节标题（如"第二部分 言语理解与表达"）
            else if (line.matches("^[第]?[一二三四五六七八九十]+[部分编章节]?\\s+.+")) {
                // 章节标题，重置当前题目状态
                currentNum = null;
                currentAnalysis.setLength(0);
                inAnalysis = false;
                logger.debug("遇到章节标题: {}", line);
            }
        }
        
        logger.info("========== 解析答案文件完成 ==========");
        logger.info("共解析到 {} 道题的答案", answerMap.size());
        if (!answerMap.isEmpty()) {
            logger.info("解析到的题号: {}", String.join(", ", answerMap.keySet()));
            // 打印详细信息
            for (String num : answerMap.keySet()) {
                AnswerAnalysis aa = answerMap.get(num);
                logger.info("题目 {}: 答案={}, 解析长度={}", num, aa.getAnswer(), 
                    aa.getAnalysis() != null ? aa.getAnalysis().length() : 0);
            }
        }
        
        return answerMap;
    }
    
    /**
     * 保存当前题目的解析内容
     */
    private void saveCurrentAnalysis(Map<String, AnswerAnalysis> answerMap, String currentNum, 
                                     StringBuilder currentAnalysis) {
        if (currentNum != null && currentAnalysis.length() > 0) {
            AnswerAnalysis aa = answerMap.get(currentNum);
            if (aa != null) {
                String existingAnalysis = aa.getAnalysis();
                if (existingAnalysis != null && !existingAnalysis.isEmpty()) {
                    aa.setAnalysis(existingAnalysis + " " + currentAnalysis.toString().trim());
                } else {
                    aa.setAnalysis(currentAnalysis.toString().trim());
                }
                logger.debug("保存题目 {} 的解析，长度={}", currentNum, aa.getAnalysis().length());
            }
            currentAnalysis.setLength(0);
        }
    }
    
    /**
     * 从行中提取题号
     */
    private String extractNumberFromLine(String line) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("(\\d+)");
        java.util.regex.Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1);
        }
        return "";
    }
    
    /**
     * 从行中提取答案
     */
    private String extractAnswerFromLine(String line) {
        java.util.regex.Pattern pattern = java.util.regex.Pattern.compile("([A-Ea-e]+)");
        java.util.regex.Matcher matcher = pattern.matcher(line);
        if (matcher.find()) {
            return matcher.group(1).toUpperCase();
        }
        return "";
    }
    
    /**
     * 从行中提取解析内容（移除题号和答案后）
     */
    private String extractAnalysisFromLine(String line, String answer) {
        // 移除题号部分
        String result = line.replaceAll("\\d+[.．、,，:：]\\s*", "");
        // 移除答案标记
        result = result.replaceAll("^[答案]+[0-9]*[：:]?\\s*", "");
        result = result.replaceAll("^第\\d+题[：:]?\\s*", "");
        result = result.replaceAll("^【答案】\\s*", "");
        result = result.replaceAll("^参考答案[：:]?\\s*", "");
        // 移除答案字母
        result = result.replaceAll("^[A-Ea-e]\\s*", "");
        result = result.replaceAll("\\s*[A-Ea-e]\\s*$", "");
        return result.trim();
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
        
        // 第一步：按题号精确匹配
        for (ParsedQuestionDTO question : questions) {
            String num = String.valueOf(question.getOrderNum());
            if (answerMap.containsKey(num)) {
                AnswerAnalysis aa = answerMap.get(num);
                question.setCorrectAnswer(aa.getAnswer());
                question.setAnalysis(aa.getAnalysis());
                logger.debug("题目 {} 按题号匹配到答案: {}", num, aa.getAnswer());
                matchedCount++;
            }
        }
        
        logger.info("按题号精确匹配完成: 匹配 {} 道", matchedCount);
        
        // 如果精确匹配的数量较少（少于一半），尝试按顺序匹配
        if (matchedCount < questions.size() / 2 && !answerMap.isEmpty()) {
            logger.info("精确匹配数量较少，尝试按顺序匹配...");
            
            // 将答案按题号排序
            List<String> sortedAnswerNums = new ArrayList<>(answerMap.keySet());
            sortedAnswerNums.sort((a, b) -> {
                try {
                    return Integer.compare(Integer.parseInt(a), Integer.parseInt(b));
                } catch (NumberFormatException e) {
                    return a.compareTo(b);
                }
            });
            
            // 按顺序匹配：答案文件中的第N条答案对应题目文件中的第N题
            int orderIndex = 0;
            for (ParsedQuestionDTO question : questions) {
                // 如果已经有答案，跳过
                if (question.getCorrectAnswer() != null && !question.getCorrectAnswer().isEmpty()) {
                    continue;
                }
                
                if (orderIndex < sortedAnswerNums.size()) {
                    String answerNum = sortedAnswerNums.get(orderIndex);
                    AnswerAnalysis aa = answerMap.get(answerNum);
                    question.setCorrectAnswer(aa.getAnswer());
                    question.setAnalysis(aa.getAnalysis());
                    logger.debug("题目 {} 按顺序匹配到答案(原题号{}): {}", 
                        question.getOrderNum(), answerNum, aa.getAnswer());
                    matchedCount++;
                }
                orderIndex++;
            }
        }
        
        unmatchedCount = questions.size() - matchedCount;
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
    
    /**
     * 提取分类标题（支持多种格式）
     * 支持格式：
     * - 第一部分 常识判断
     * - 第二编 数量关系
     * - 第三章 言语理解与表达
     * - 常识判断
     * - 第一部分 常识判断 (共20题)
     */
    private String extractCategory(String line) {
        // 模式1：第X部分/编/章 标题
        Pattern pattern1 = Pattern.compile("^第[一二三四五六七八九十\\d]+(部分|编|章)\\s+(.+)");
        Matcher matcher1 = pattern1.matcher(line);
        if (matcher1.find()) {
            String category = matcher1.group(2).trim();
            // 移除末尾的题数信息，如"(共20题)"
            category = category.replaceAll("\\(共\\d+题\\)$", "").trim();
            return category;
        }
        
        // 模式2：单纯的分类名称（匹配系统支持的分类）
        String[] knownCategories = {
            "政治理论", "数量关系", "资料分析", "常识判断", 
            "判断推理", "言语理解", "言语理解与表达"
        };
        for (String cat : knownCategories) {
            if (line.contains(cat)) {
                return cat;
            }
        }
        
        return null;
    }

    /**
     * 导入题目到 test 表（测试导入功能）
     */
    @PostMapping("/batch-import/test")
    public ResponseEntity<ApiResponse<Map<String, Object>>> importToTestTable(
            @RequestParam("questionFile") MultipartFile questionFile,
            @RequestParam(value = "answerFile", required = false) MultipartFile answerFile,
            @RequestParam(value = "category", required = false) String category) {
        
        logger.info("收到导入到 test 表请求: questionFile={}, answerFile={}, category={}",
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
            
            // 合并所有题目用于导入
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
            
            // 5. 导入到 test 表
            Map<String, Object> importResult = testImportService.importQuestionsToTest(allQuestions);
            
            logger.info("导入到 test 表完成: total={}, saved={}", 
                        allQuestions.size(), importResult.get("saved"));
            
            return ResponseEntity.ok(ApiResponse.success(importResult));
            
        } catch (Exception e) {
            logger.error("导入到 test 表失败", e);
            return ResponseEntity.ok(ApiResponse.error("导入失败: " + e.getMessage()));
        }
    }

    /**
     * 清空 test 表
     */
    @DeleteMapping("/batch-import/test")
    public ResponseEntity<ApiResponse<String>> clearTestTable() {
        try {
            testImportService.clearTestTable();
            logger.info("已清空 test 表");
            return ResponseEntity.ok(ApiResponse.success("已清空 test 表"));
        } catch (Exception e) {
            logger.error("清空 test 表失败", e);
            return ResponseEntity.ok(ApiResponse.error("清空失败: " + e.getMessage()));
        }
    }

    /**
     * 获取 test 表中的题目数量
     */
    @GetMapping("/batch-import/test/count")
    public ResponseEntity<ApiResponse<Map<String, Object>>> getTestTableCount() {
        try {
            long count = testImportService.getTestTableCount();
            Map<String, Object> result = new HashMap<>();
            result.put("count", count);
            return ResponseEntity.ok(ApiResponse.success(result));
        } catch (Exception e) {
            logger.error("获取 test 表数量失败", e);
            return ResponseEntity.ok(ApiResponse.error("获取失败: " + e.getMessage()));
        }
    }

    /**
     * 保存题目到正式的 questions_total 表
     */
    @PostMapping("/batch-import/save")
    public ResponseEntity<ApiResponse<Map<String, Object>>> saveQuestions(
            @RequestBody ImportRequestDTO request) {
        
        List<ParsedQuestionDTO> questions = request.getQuestions();
        String source = request.getSource();
        
        logger.info("收到保存题目请求，共 {} 道题目，来源: {}", questions.size(), source);
        
        try {
            // 将题目导入到 test 表进行测试
            Map<String, Object> importResult = testImportService.importQuestionsToTest(questions, source);
            
            logger.info("题目保存到 test 表完成: total={}, saved={}", 
                        questions.size(), importResult.get("saved"));
            
            return ResponseEntity.ok(ApiResponse.success(importResult));
            
        } catch (Exception e) {
            logger.error("保存题目失败", e);
            return ResponseEntity.ok(ApiResponse.error("保存失败: " + e.getMessage()));
        }
    }
}
