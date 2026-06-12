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
    @Query("SELECT COUNT(up) FROM UserPractice up WHERE up.user.id = :userId")
    long countByUserId(@Param("userId") Long userId);

    /**
     * 查询用户正确数
     */
    @Query("SELECT COUNT(up) FROM UserPractice up WHERE up.user.id = :userId AND up.isCorrect = true")
    long countCorrectByUserId(@Param("userId") Long userId);

    /**
     * 查询每日提交数
     */
    @Query("SELECT CAST(up.createdAt AS DATE) as day, COUNT(up) as count FROM UserPractice up WHERE up.createdAt >= :startDate GROUP BY CAST(up.createdAt AS DATE) ORDER BY day")
    java.util.List<Object[]> countDailySubmissions(@Param("startDate") LocalDateTime startDate);

    /**
     * 查询各分类的正确数
     */
    @Query("SELECT up.question.categoryId, COUNT(up) FROM UserPractice up WHERE up.user.id = :userId AND up.isCorrect = true GROUP BY up.question.categoryId")
    java.util.List<Object[]> countCorrectByCategory(@Param("userId") Long userId);

    /**
     * 查询刷题排行榜
     */
    @Query("SELECT up.user.id, up.user.username, COUNT(up) as solved FROM UserPractice up GROUP BY up.user.id, up.user.username ORDER BY solved DESC")
    java.util.List<Object[]> findRanking();

    /**
     * 查询总正确数
     */
    @Query("SELECT COUNT(up) FROM UserPractice up WHERE up.isCorrect = true")
    long countAllCorrect();

    /**
     * 查询各分类的总提交数和正确数
     */
    @Query("SELECT up.question.categoryName, COUNT(up), SUM(CASE WHEN up.isCorrect THEN 1 ELSE 0 END) FROM UserPractice up GROUP BY up.question.categoryName")
    java.util.List<Object[]> countByCategory();

    /**
     * 查询本周新增用户数
     */
    @Query("SELECT COUNT(DISTINCT up.user.id) FROM UserPractice up WHERE up.createdAt >= :startDate")
    long countActiveUsersSince(@Param("startDate") LocalDateTime startDate);
}
