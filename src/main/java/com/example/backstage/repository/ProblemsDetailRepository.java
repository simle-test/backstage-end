package com.example.backstage.repository;

import com.example.backstage.entity.ProblemsDetail;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 题目答题详情数据访问层
 */
@Repository
public interface ProblemsDetailRepository extends JpaRepository<ProblemsDetail, Integer> {

    /**
     * 按模块统计答题总数和正确数
     * 通过question_id关联questions_total表获取category_name
     * 返回结果: [categoryName, totalCount, correctCount]
     */
    @Query(value = "SELECT q.\"category_name\", COUNT(pd), SUM(CASE WHEN pd.\"statue\" = 1 THEN 1 ELSE 0 END) FROM \"problems_detail\" pd LEFT JOIN \"questions_total\" q ON pd.\"question_id\" = q.\"id\" GROUP BY q.\"category_name\"", nativeQuery = true)
    List<Object[]> countByCategoryName();

    /**
     * 统计所有答题记录中正确的数量
     */
    @Query("SELECT COUNT(pd) FROM ProblemsDetail pd WHERE pd.statue = 1")
    long countAllCorrect();

    /**
     * 统计所有答题记录总数
     */
    long count();
}