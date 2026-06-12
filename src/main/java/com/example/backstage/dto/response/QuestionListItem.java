package com.example.backstage.dto.response;

import java.util.List;

/**
 * 题目列表项
 */
public class QuestionListItem {
    private Integer id;
    private String questionId;
    private String title;
    private String content;
    private Object options;
    private String questionContent;
    private String answer;
    private String analysis;
    private String difficulty;
    private String difficultyText;
    private String category;
    private String categoryText;
    private Integer bankId;
    private String bankName;
    private Integer materialId;
    private String imageUrl;
    private String createdAt;

    public QuestionListItem() {}

    public QuestionListItem(Integer id, String questionId, String title, String content, Object options,
                           String questionContent, String answer, String analysis, String difficulty,
                           String difficultyText, String category, String categoryText, Integer bankId,
                           String bankName, Integer materialId, String imageUrl, String createdAt) {
        this.id = id;
        this.questionId = questionId;
        this.title = title;
        this.content = content;
        this.options = options;
        this.questionContent = questionContent;
        this.answer = answer;
        this.analysis = analysis;
        this.difficulty = difficulty;
        this.difficultyText = difficultyText;
        this.category = category;
        this.categoryText = categoryText;
        this.bankId = bankId;
        this.bankName = bankName;
        this.materialId = materialId;
        this.imageUrl = imageUrl;
        this.createdAt = createdAt;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public Object getOptions() { return options; }
    public void setOptions(Object options) { this.options = options; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getDifficultyText() { return difficultyText; }
    public void setDifficultyText(String difficultyText) { this.difficultyText = difficultyText; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getCategoryText() { return categoryText; }
    public void setCategoryText(String categoryText) { this.categoryText = categoryText; }
    public Integer getBankId() { return bankId; }
    public void setBankId(Integer bankId) { this.bankId = bankId; }
    public String getBankName() { return bankName; }
    public void setBankName(String bankName) { this.bankName = bankName; }
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
    public String getCreatedAt() { return createdAt; }
    public void setCreatedAt(String createdAt) { this.createdAt = createdAt; }
}