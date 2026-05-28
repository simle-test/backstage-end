package com.example.backstage.repository;

import com.example.backstage.entity.UserPractice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;

/**
 * 用户刷题记录数据访问层
 */
@Repository
public interface UserPracticeRepository extends JpaRepository<UserPractice, Integer> {

    /**
     * 查询用户刷题总数
     */
    long countByUserId(Integer userId);

    /**
     * 查询用户正确数
     */
    @Query("SELECT COUNT(up) FROM UserPractice up WHERE up.user.id = :userId AND up.isCorrect = true")
    long countCorrectByUserId(@Param("userId") Integer userId);

    /**
     * 查询每日提交数
     */
    @Query("SELECT DATE(up.createdAt) as day, COUNT(up) as count FROM UserPractice up WHERE up.createdAt >= :startDate GROUP BY DATE(up.createdAt)")
    Object[] countDailySubmissions(@Param("startDate") LocalDateTime startDate);

    /**
     * 查询各分类的正确数
     */
    @Query("SELECT up.question.categoryId, COUNT(up) FROM UserPractice up WHERE up.user.id = :userId AND up.isCorrect = true GROUP BY up.question.categoryId")
    Object[] countCorrectByCategory(@Param("userId") Integer userId);

    /**
     * 查询刷题排行榜
     */
    @Query("SELECT up.user.id, up.user.username, COUNT(up) as solved FROM UserPractice up GROUP BY up.user.id, up.user.username ORDER BY solved DESC")
    Object[] findRanking();
}
