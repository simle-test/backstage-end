package com.example.backstage.entity;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

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

    @Column(name = "question_id", length = 50)
    private String questionId;

    @Column(name = "question_text", columnDefinition = "text")
    private String questionText;

    @Column(name = "option_a", columnDefinition = "text")
    private String optionA;

    @Column(name = "option_b", columnDefinition = "text")
    private String optionB;

    @Column(name = "option_c", columnDefinition = "text")
    private String optionC;

    @Column(name = "option_d", columnDefinition = "text")
    private String optionD;

    @Column(name = "option_e", columnDefinition = "text")
    private String optionE;

    @Column(name = "correct_answer", length = 10)
    private String correctAnswer;

    @Column(name = "analysis", columnDefinition = "text")
    private String analysis;

    @Column(name = "order_num")
    private Integer orderNum;

    @Column(name = "difficulty", length = 10)
    private String difficulty;

    @Column(name = "status", length = 20)
    private String status;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    // Getters and Setters
    public Integer getId() { return id; }
    public void setId(Integer id) { this.id = id; }
    public Integer getMaterialId() { return materialId; }
    public void setMaterialId(Integer materialId) { this.materialId = materialId; }
    public String getQuestionId() { return questionId; }
    public void setQuestionId(String questionId) { this.questionId = questionId; }
    public String getQuestionText() { return questionText; }
    public void setQuestionText(String questionText) { this.questionText = questionText; }
    public String getOptionA() { return optionA; }
    public void setOptionA(String optionA) { this.optionA = optionA; }
    public String getOptionB() { return optionB; }
    public void setOptionB(String optionB) { this.optionB = optionB; }
    public String getOptionC() { return optionC; }
    public void setOptionC(String optionC) { this.optionC = optionC; }
    public String getOptionD() { return optionD; }
    public void setOptionD(String optionD) { this.optionD = optionD; }
    public String getOptionE() { return optionE; }
    public void setOptionE(String optionE) { this.optionE = optionE; }
    public String getCorrectAnswer() { return correctAnswer; }
    public void setCorrectAnswer(String correctAnswer) { this.correctAnswer = correctAnswer; }
    public String getAnalysis() { return analysis; }
    public void setAnalysis(String analysis) { this.analysis = analysis; }
    public Integer getOrderNum() { return orderNum; }
    public void setOrderNum(Integer orderNum) { this.orderNum = orderNum; }
    public String getDifficulty() { return difficulty; }
    public void setDifficulty(String difficulty) { this.difficulty = difficulty; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
    public void setUpdatedAt(LocalDateTime updatedAt) { this.updatedAt = updatedAt; }
}
