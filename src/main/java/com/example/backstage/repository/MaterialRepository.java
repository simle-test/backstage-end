package com.example.backstage.repository;

import com.example.backstage.entity.Material;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

/**
 * 材料数据访问层
 */
@Repository
public interface MaterialRepository extends JpaRepository<Material, Integer> {

    /**
     * 根据材料ID查询材料
     */
    Material findByMaterialId(Integer materialId);
}
