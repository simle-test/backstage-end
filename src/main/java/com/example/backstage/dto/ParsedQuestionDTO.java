package com.example.backstage.dto;

import java.util.List;

public class ParsedQuestionDTO {
    private String type; // 题目类型：single_choice, multiple_choice, fill_blank, true_false, essay
    private String category; // 题目分类：political_theory, quantity_relation, material_analysis, common_sense_judgment, logical_judgment, language_understanding
    private String title; // 题干
    private List<String> options; // 选项列表
    private String correctAnswer; // 正确答案
    private String analysis; // 答案解析
    private String difficulty; // 难度：easy, medium, hard
    private Integer orderNum; // 题目序号

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public List<String> getOptions() {
        return options;
    }

    public void setOptions(List<String> options) {
        this.options = options;
    }

    public String getCorrectAnswer() {
        return correctAnswer;
    }

    public void setCorrectAnswer(String correctAnswer) {
        this.correctAnswer = correctAnswer;
    }

    public String getAnalysis() {
        return analysis;
    }

    public void setAnalysis(String analysis) {
        this.analysis = analysis;
    }

    public String getDifficulty() {
        return difficulty;
    }

    public void setDifficulty(String difficulty) {
        this.difficulty = difficulty;
    }

    public Integer getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Integer orderNum) {
        this.orderNum = orderNum;
    }
}