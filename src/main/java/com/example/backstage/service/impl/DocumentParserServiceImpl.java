package com.example.backstage.service.impl;

import com.example.backstage.dto.ParsedQuestionDTO;
import com.example.backstage.service.DocumentParserService;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.apache.poi.xwpf.usermodel.XWPFParagraph;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
public class DocumentParserServiceImpl implements DocumentParserService {

    private static final Logger log = LoggerFactory.getLogger(DocumentParserServiceImpl.class);

    private static final Pattern QUESTION_PATTERN = Pattern.compile(
        "(\\d+[\\.、\\s]*)([^\\d]*?)(?=\\d+[\\.、\\s]|$)", Pattern.DOTALL
    );

    private static final Pattern OPTION_PATTERN = Pattern.compile(
        "([A-F])[\\.、\\s]+([^A-F]*?)(?=[A-F][\\.、\\s]|$)", Pattern.DOTALL
    );

    private static final Pattern SINGLE_CHOICE_PATTERN = Pattern.compile(
        "(单选题|单项选择|选择题)"
    );

    private static final Pattern MULTIPLE_CHOICE_PATTERN = Pattern.compile(
        "(多选题|多项选择)"
    );

    private static final Pattern FILL_BLANK_PATTERN = Pattern.compile(
        "(填空题|填空)"
    );

    private static final Pattern TRUE_FALSE_PATTERN = Pattern.compile(
        "(判断题|判断|对错题)"
    );

    private static final Pattern ESSAY_PATTERN = Pattern.compile(
        "(解答题|简答题|论述题|分析题|问答题)"
    );

    @Override
    public List<ParsedQuestionDTO> parseDocument(MultipartFile file, String category) {
        try {
            String fileName = file.getOriginalFilename();
            if (fileName == null) {
                throw new IllegalArgumentException("文件名不能为空");
            }

            String content;
            if (fileName.endsWith(".docx")) {
                content = parseWordDocument(file);
            } else if (fileName.endsWith(".pdf")) {
                content = parsePdfDocument(file);
            } else if (fileName.endsWith(".txt")) {
                content = new String(file.getBytes());
            } else {
                throw new IllegalArgumentException("不支持的文件格式，仅支持 .docx, .pdf, .txt");
            }

            return extractQuestions(content, category);
        } catch (IOException e) {
            log.error("解析文档失败", e);
            throw new RuntimeException("解析文档失败: " + e.getMessage());
        }
    }

    private String parseWordDocument(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             XWPFDocument document = new XWPFDocument(is)) {

            StringBuilder content = new StringBuilder();
            for (XWPFParagraph paragraph : document.getParagraphs()) {
                String text = paragraph.getText().trim();
                if (!text.isEmpty()) {
                    content.append(text).append("\n");
                }
            }
            return content.toString();
        }
    }

    private String parsePdfDocument(MultipartFile file) throws IOException {
        try (InputStream is = file.getInputStream();
             PDDocument document = Loader.loadPDF(is.readAllBytes())) {

            PDFTextStripper stripper = new PDFTextStripper();
            stripper.setSortByPosition(true);
            return stripper.getText(document);
        }
    }

    private List<ParsedQuestionDTO> extractQuestions(String content, String category) {
        List<ParsedQuestionDTO> questions = new ArrayList<>();
        String[] lines = content.split("\n");
        StringBuilder currentQuestion = new StringBuilder();
        int questionNumber = 1;

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) {
                continue;
            }

            // 检查是否是新题目的开始
            if (isQuestionStart(line)) {
                // 保存上一题
                if (currentQuestion.length() > 0) {
                    ParsedQuestionDTO question = parseQuestion(currentQuestion.toString(), questionNumber++, category);
                    if (question != null) {
                        questions.add(question);
                    }
                }
                currentQuestion = new StringBuilder(line);
            } else {
                currentQuestion.append("\n").append(line);
            }
        }

        // 保存最后一题
        if (currentQuestion.length() > 0) {
            ParsedQuestionDTO question = parseQuestion(currentQuestion.toString(), questionNumber, category);
            if (question != null) {
                questions.add(question);
            }
        }

        return questions;
    }

    private boolean isQuestionStart(String line) {
        // 匹配数字开头的行，如 "1."、"1、"、"1、" 等
        return line.matches("^\\d+[\\.、\\s]+.*");
    }

    private ParsedQuestionDTO parseQuestion(String content, int orderNum, String category) {
        ParsedQuestionDTO question = new ParsedQuestionDTO();

        // 识别题目类型
        String type = detectQuestionType(content);
        question.setType(type);
        question.setCategory(category);
        question.setOrderNum(orderNum);
        question.setDifficulty("medium"); // 默认中等难度

        // 提取题干和选项
        Object[] result = extractTitleAndOptions(content);
        String title = (String) result[0];
        List<String> options = (List<String>) result[1];

        question.setTitle(title);
        question.setOptions(options);

        // 答案和解析由AI生成，这里暂时留空
        question.setCorrectAnswer("");
        question.setAnalysis("");

        return question;
    }

    @Override
    public String detectQuestionType(String content) {
        if (MULTIPLE_CHOICE_PATTERN.matcher(content).find()) {
            return "multiple_choice";
        } else if (SINGLE_CHOICE_PATTERN.matcher(content).find()) {
            return "single_choice";
        } else if (FILL_BLANK_PATTERN.matcher(content).find()) {
            return "fill_blank";
        } else if (TRUE_FALSE_PATTERN.matcher(content).find()) {
            return "true_false";
        } else if (ESSAY_PATTERN.matcher(content).find()) {
            return "essay";
        } else {
            // 默认为单选题
            return "single_choice";
        }
    }

    @Override
    public Object[] extractTitleAndOptions(String content) {
        String title = content;
        List<String> options = new ArrayList<>();

        // 移除题号
        title = title.replaceFirst("^\\d+[\\.、\\s]+", "").trim();

        // 提取选项
        Matcher matcher = OPTION_PATTERN.matcher(title);
        StringBuilder titleBuilder = new StringBuilder();
        int lastEnd = 0;

        while (matcher.find()) {
            // 添加选项前的题干部分
            titleBuilder.append(title.substring(lastEnd, matcher.start()));

            String optionLabel = matcher.group(1);
            String optionContent = matcher.group(2).trim();
            options.add(optionContent);

            lastEnd = matcher.end();
        }

        // 添加剩余的题干部分
        if (lastEnd < title.length()) {
            titleBuilder.append(title.substring(lastEnd));
        }

        // 如果没有找到选项，检查是否是选择题（通过是否有A、B、C、D等选项标记）
        if (options.isEmpty()) {
            String[] lines = title.split("\n");
            StringBuilder newTitle = new StringBuilder();
            for (String line : lines) {
                line = line.trim();
                if (line.matches("^[A-F][\\.、\\s]+.*")) {
                    // 这是选项行
                    String optionContent = line.replaceFirst("^[A-F][\\.、\\s]+", "").trim();
                    options.add(optionContent);
                } else if (!line.isEmpty()) {
                    // 这是题干行
                    if (newTitle.length() > 0) {
                        newTitle.append(" ");
                    }
                    newTitle.append(line);
                }
            }
            if (newTitle.length() > 0) {
                title = newTitle.toString();
            }
        } else {
            title = titleBuilder.toString().trim();
        }

        return new Object[]{title, options};
    }
}