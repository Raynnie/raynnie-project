package com.raynnie.bookcatalog.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 11:50
 *
 * 图书分类实体类，对应数据库中的 categories 表
 * 用于组织和管理图书的分类体系
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "categories")
public class Category {
    /**
     * 分类ID，主键，自动生成
     * 用于唯一标识系统中的每个分类
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 分类名称，不能为空，必须唯一
     * 例如："计算机科学"、"文学"
     */
    @Column(nullable = false, unique = true)
    private String name;

    /**
     * 分类描述，可选，最大长度500字符
     * 用于详细说明分类的范围和特点
     */
    @Column(length = 500)
    private String description;

    /**
     * 属于该分类的图书集合
     * 双向关联Book实体，由Book端维护关联关系
     */
    @ManyToMany(mappedBy = "categories")
    private Set<Book> books = new HashSet<>();
}
