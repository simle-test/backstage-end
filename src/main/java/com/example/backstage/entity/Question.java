package com.example.backstage.entity;

import jakarta.persistence.*;
import lombok.Data;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.annotations.UpdateTimestamp;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;

/**
 * 题目实体
 */
@Data
@Entity
@Table(name = "questions_total")
public class Question {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "question_id", length = 20)
    private String questionId;

    @Column(name = "title", columnDefinition = "text")
    private String title;

    @Column(name = "category_id", length = 50)
    private String categoryId;

    @Column(name = "category_name", length = 50)
    private String categoryName;

    @Column(name = "difficulty", length = 10)
    private String difficulty;

    @Column(name = "year")
    private Integer year;

    @Column(name = "source", length = 255)
    private String source;

    @Column(name = "status", length = 20)
    private String status;

    @Column(name = "question_content", columnDefinition = "jsonb")
    @JdbcTypeCode(SqlTypes.JSON)
    private String questionContent;

    @Column(name = "correct_answer", length = 10)
    private String correctAnswer;

    @Column(name = "answer_analysis", columnDefinition = "text")
    private String answerAnalysis;

    @Column(name = "tips", columnDefinition = "text")
    private String tips;

    @Column(name = "has_image")
    private Boolean hasImage;

    @Column(name = "image_url", length = 255)
    private String imageUrl;

    @Column(name = "has_material")
    private Boolean hasMaterial;

    @Column(name = "material_id")
    private Integer materialId;

    @Column(name = "content_text", columnDefinition = "text")
    private String contentText;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
