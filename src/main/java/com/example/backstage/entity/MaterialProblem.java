package com.example.backstage.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

/**
 * 材料小题实体
 * 资料分析题的选择题存储在此表中
 */
@Entity
@Table(name = "material_problems")
public class MaterialProblem {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "material_id")
    private Integer materialId;

    @Column(name = "title", columnDefinition = "text")
    private String title;

    @Column(name = "question_content", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String questionContent;

    @Column(name = "correct_answer", length = 10)
    private String correctAnswer;

    @Column(name = "answer_analysis", columnDefinition = "text")
    private String answerAnalysis;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getQuestionContent() { return questionContent; }
    public void setQuestionContent(String questionContent) { this.questionContent = questionContent; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getAnswerAnalysis() { return answerAnalysis; }
    public void setAnswerAnalysis(String answerAnalysis) { this.answerAnalysis = answerAnalysis; }
}
