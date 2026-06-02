package com.example.backstage.repository;

import com.example.backstage.entity.MaterialProblem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

/**
 * 材料小题数据访问层
 */
@Repository
public interface MaterialProblemRepository extends JpaRepository<MaterialProblem, Integer> {

    /**
     * 根据材料ID查询所有小题，按序号排序
     */
    List<MaterialProblem> findByMaterialIdOrderByOrderNumAsc(Integer materialId);

    /**
     * 根据材料ID查询小题数量
     */
    long countByMaterialId(Integer materialId);

    /**
     * 根据材料ID删除所有小题
     */
    void deleteByMaterialId(Integer materialId);

    /**
     * 根据问题ID查询小题
     */
    MaterialProblem findByQuestionId(String questionId);
}
