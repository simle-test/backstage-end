package com.example.backstage.dto.response;

import java.util.List;

/**
 * 题目详情响应
 */
public class QuestionDetailResponse {
    private Integer id;
    private String title;
    private String content;
    private List<String> options;
    private String answer;
    private String difficulty;
    private String difficultyText;
    private String category;
    private String categoryId;
    private String analysis;
    private Double passRate;
    private String status;
    private String statusText;
    private String createTime;
    private String updateTime;
    private String questionContent;
    private String source;
    private Integer year;
    private Integer materialId;
    private String materialTitle;
    private String materialContent;
    private String materialImageUrl;
    private String imageUrl;

    public QuestionDetailResponse() {}

    public QuestionDetailResponse(Integer id, String title, String content, List<String> options, String answer,
                                 String difficulty, String difficultyText, String category, String analysis,
                                 Double passRate, String status, String statusText, String createTime, String updateTime) {
        this.id = id;
        this.title = title;
        this.content = content;
        this.options = options;
        this.answer = answer;
        this.difficulty = difficulty;
        this.difficultyText = difficultyText;
        this.category = category;
        this.analysis = analysis;
        this.passRate = passRate;
        this.status = status;
        this.statusText = statusText;
        this.createTime = createTime;
        this.updateTime = updateTime;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getContent() { return content; }
    public void setContent(String content) { this.content = content; }
    public List<String> getOptions() { return options; }
    public void setOptions(List<String> options) { this.options = options; }
    public String getAnswer() { return answer; }
    public void setAnswer(String answer) { this.answer = answer; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getDifficultyText() { return difficultyText; }
    public void setDifficultyText(String difficultyText) { this.difficultyText = difficultyText; }
    public String getCategory() { return category; }
    public void setCategory(String category) { this.category = category; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public Double getPassRate() { return passRate; }
    public void setPassRate(Double passRate) { this.passRate = passRate; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public String getStatusText() { return statusText; }
    public void setStatusText(String statusText) { this.statusText = statusText; }
    public String getCreateTime() { return createTime; }
    public void setCreateTime(String createTime) { this.createTime = createTime; }
    public String getUpdateTime() { return updateTime; }
    public void setUpdateTime(String updateTime) { this.updateTime = updateTime; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
    public Integer getYear() { return year; }
    public void setYear(Integer year) { this.year = year; }
    public String getCategoryId() { return categoryId; }
    public void setCategoryId(String categoryId) { this.categoryId = categoryId; }
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    public String getMaterialTitle() { return materialTitle; }
    public void setMaterialTitle(String materialTitle) { this.materialTitle = materialTitle; }
    public String getMaterialContent() { return materialContent; }
    public void setMaterialContent(String materialContent) { this.materialContent = materialContent; }
    public String getMaterialImageUrl() { return materialImageUrl; }
    public void setMaterialImageUrl(String materialImageUrl) { this.materialImageUrl = materialImageUrl; }
    public String getImageUrl() { return imageUrl; }
    public void setImageUrl(String imageUrl) { this.imageUrl = imageUrl; }
}
