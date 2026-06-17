package com.example.backstage.dto;

import java.time.LocalDateTime;

public class DuplicateDetail {
    
    private Integer id;
    
    private String questionId;
    
    private String title;
    
    private String correctAnswer;
    
    private String categoryName;
    
    private String source;
    
    private LocalDateTime createdAt;
    
    private Double similarityScore;
    
    private Integer duplicateOfId;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getQuestionId() {
        return questionId;
    }

    public void setQuestionId(String questionId) {
        this.questionId = questionId;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

    public String getSource() {
        return source;
    }

    public void setSource(String source) {
        this.source = source;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public Double getSimilarityScore() {
        return similarityScore;
    }

    public void setSimilarityScore(Double similarityScore) {
        this.similarityScore = similarityScore;
    }

    public Integer getDuplicateOfId() {
        return duplicateOfId;
    }

    public void setDuplicateOfId(Integer duplicateOfId) {
        this.duplicateOfId = duplicateOfId;
    }
}