package com.example.backstage.repository;

import com.example.backstage.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

/**
 * 题目数据访问层
 */
@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {

    /**
     * 分页查询题目
     */
    Page<Question> findByCategoryIdContainingAndDifficultyContainingAndTitleContaining(
        String categoryId, String difficulty, String title, Pageable pageable);

    /**
     * 查询不同难度的题目数量
     */
    @Query("SELECT q.difficulty, COUNT(q) FROM Question q GROUP BY q.difficulty")
    Object[] countByDifficulty();

    /**
     * 根据分类ID查询题目数量
     */
    long countByCategoryId(String categoryId);

    /**
     * 根据分类ID模糊查询题目数量
     */
    long countByCategoryIdContaining(String categoryId);

    /**
     * 按难度统计题目数量
     */
    long countByDifficulty(String difficulty);

    /**
     * 根据材料ID查询题目列表
     */
    java.util.List<Question> findByMaterialId(Integer materialId);

    /**
     * 查询有材料ID的题目（材料分析题）
     */
    java.util.List<Question> findByMaterialIdIsNotNull();

    /**
     * 根据难度和标题查询题目（不带分类）
     */
    org.springframework.data.domain.Page<Question> findByDifficultyContainingAndTitleContaining(
        String difficulty, String title, org.springframework.data.domain.Pageable pageable);

    /**
     * 查询材料分析题（has_material=true 或者 image_url不为空）
     */
    @Query("SELECT q FROM Question q WHERE q.hasMaterial = true OR (q.imageUrl IS NOT NULL AND q.imageUrl != '')")
    java.util.List<Question> findMaterialAnalysisQuestions();

    /**
     * 统计今日新增题目数量
     */
    @Query("SELECT COUNT(q) FROM Question q WHERE q.createdAt >= :startOfDay")
    long countByCreatedAtAfter(java.time.LocalDateTime startOfDay);

    /**
     * 查询指定时间之后创建的题目（分页）
     */
    Page<Question> findByCreatedAtAfter(java.time.LocalDateTime startTime, Pageable pageable);

    /**
     * 查询指定分类和时间之后创建的题目（分页）
     */
    Page<Question> findByCategoryIdAndCreatedAtAfter(String categoryId, java.time.LocalDateTime startTime, Pageable pageable);

    /**
     * 统计指定分类和时间之后创建的题目数量
     */
    long countByCategoryIdAndCreatedAtAfter(String categoryId, java.time.LocalDateTime startTime);

    /**
     * 统计指定分类和时间范围内创建的题目数量
     */
    long countByCategoryIdAndCreatedAtBetween(String categoryId, java.time.LocalDateTime startTime, java.time.LocalDateTime endTime);
}