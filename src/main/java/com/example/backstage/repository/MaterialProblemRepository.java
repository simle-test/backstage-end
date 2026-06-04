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
     * 根据材料ID查询小题列表，按id排序
     */
    List<MaterialProblem> findByMaterialIdOrderByIdAsc(Integer materialId);
}