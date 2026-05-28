package com.example.backstage.repository;

import com.example.backstage.entity.Question;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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
     * 按难度统计题目数量
     */
    long countByDifficulty(String difficulty);
}
