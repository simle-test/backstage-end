package com.example.backstage.dto.response;

import java.util.List;

/**
 * 题库详情响应
 */
public class BankDetailResponse {
    private Integer id;
    private String name;
    private String desc;
    private String color;
    private List<QuestionListItem> questions;
    private Long count;

    public BankDetailResponse() {}

    public BankDetailResponse(Integer id, String name, String desc, String color, List<QuestionListItem> questions, Long count) {
        this.id = id;
        this.name = name;
        this.desc = desc;
        this.color = color;
        this.questions = questions;
        this.count = count;
    }

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getDesc() { return desc; }
    public void setDesc(String desc) { this.desc = desc; }
    public String getColor() { return color; }
    public void setColor(String color) { this.color = color; }
    public List<QuestionListItem> getQuestions() { return questions; }
    public void setQuestions(List<QuestionListItem> questions) { this.questions = questions; }
    public Long getCount() { return count; }
    public void setCount(Long count) { this.count = count; }
}
