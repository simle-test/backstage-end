package com.example.backstage.entity;

import jakarta.persistence.*;

/**
 * 题目答题详情实体
 * 存储用户答题的详细记录，statue字段表示答题状态
 */
@Entity
@Table(name = "problems_detail")
public class ProblemsDetail {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "question_id")
    private Integer questionId;

    @Column(name = "user_id")
    private Integer userId;

    @Column(name = "statue")
    private Integer statue;

    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(name = "category_name", length = 50)
    private String categoryName;

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getQuestionId() {
        return questionId;
    }

    public void setQuestionId(Integer questionId) {
        this.questionId = questionId;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getStatue() {
        return statue;
    }

    public void setStatue(Integer statue) {
        this.statue = statue;
    }

    public String getCategoryId() {
        return categoryId;
    }

    public void setCategoryId(String categoryId) {
        this.categoryId = categoryId;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }
}