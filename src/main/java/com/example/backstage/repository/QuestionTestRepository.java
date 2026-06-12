package com.example.backstage.repository;

import com.example.backstage.entity.QuestionTest;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 测试题目数据访问层
 */
@Repository
public interface QuestionTestRepository extends JpaRepository<QuestionTest, Integer> {
}