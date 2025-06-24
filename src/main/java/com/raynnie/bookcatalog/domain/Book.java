package com.raynnie.bookcatalog.domain;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 11:29
 *
 * 图书实体类，对应数据库中的 books 表
 * 包含图书的基本信息和业务属性，用于ORM映射和业务逻辑处理
 */

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder(builderMethodName = "buildBook")
@Entity
@Table(name = "books")
public class Book {
    /**
     * 图书ID，主键，自动生成，用于唯一标识系统中的每本图书
     */
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * 图书标题，不能为空，最大长度200字。例如：《Java核心技术》
     */
    @NotBlank(message = "书名不能为空")
    @Size(max = 200, message = "书名长度不能超过200字符")
    private String title;

    /**
     * 图书作者，不能为空，最大长度100字符
     * 多个作者可使用逗号分隔，例如："Cay S. Horstmann, Gary Cornell"
     */
    @NotBlank(message = "作者不能为空")
    @Size(max = 100, message = "作者长度不能超过100字符")
    private String author;

    /**
     * 图书价格，使用BigDecimal确保精确计算
     * 例如：99.99，表示99元9角9分
     */
    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    /**
     * 出版日期
     * 记录图书首次出版的日期
     */
    private LocalDate publishDate;

    /**
     * 国际标准书号(ISBN)：用于唯一标识图书出版物，格式可以是ISBN-10或ISBN-13
     * 例如：978-7-100-16504-4
     */
    @Column(nullable = false, unique = true)
    @Size(min = 10, max = 17)  // 兼容带横杠的格式
    private String isbn;

    /**
     * 图书状态：可选值：AVAILABLE(可借阅), UNAVAILABLE(已借出), DISCONTINUED(下架)
     */
    @Enumerated(EnumType.STRING)
    private BookStatus status;

    /**
     * 图书分类集合：一本书可属于多个分类，通过中间表book_categories关联
     * 例如：同时属于"计算机科学"和"编程"分类
     */
    @ManyToMany(fetch = FetchType.EAGER)
    @JoinTable(
            name = "book_categories",
            joinColumns = @JoinColumn(name = "book_id"),
            inverseJoinColumns = @JoinColumn(name = "category_id")
    )
    private Set<Category> categories = new HashSet<>();

    /**
     * 图书状态枚举
     * 定义图书的流通状态
     */
    public enum BookStatus {
        /** 可借阅状态 */
        AVAILABLE,
        /** 已借出状态 */
        UNAVAILABLE,
        /** 下架状态 */
        DISCONTINUED
    }
}
