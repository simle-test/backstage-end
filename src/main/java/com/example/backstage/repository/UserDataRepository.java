package com.example.backstage.repository;

import com.example.backstage.entity.UserData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * 用户数据访问层
 */
@Repository
public interface UserDataRepository extends JpaRepository<UserData, Integer> {

    /**
     * 通过data_id查询用户完成数
     */
    @Query(value = "SELECT finished FROM user_data WHERE data_id = ?1", nativeQuery = true)
    Optional<Integer> findFinishedById(Integer dataid);

    /**
     * 查询刷题排行榜（按finished字段降序）
     */
    @Query(value = "SELECT id, finished FROM user_data ORDER BY finished DESC", nativeQuery = true)
    List<Object[]> findRankingByFinish();
}
