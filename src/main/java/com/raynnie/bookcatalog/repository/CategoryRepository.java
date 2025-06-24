package com.raynnie.bookcatalog.repository;

import com.raynnie.bookcatalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 14:46
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 检查分类名称是否存在
     */
    boolean existsByName(String name);

    /**
     * 根据名称查找分类
     */
    Optional<Category> findByName(String name);
}
