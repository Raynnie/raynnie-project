package com.raynnie.bookcatalog.repository;
import com.raynnie.bookcatalog.domain.Book;
import org.springframework.data.jpa.domain.Specification;

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * 图书规格类，基于规格模式实现动态查询条件构建
 * 用于根据用户指定的条件组合生成JPA查询条件
 *
 * @Author Raynnie.J
 * @Date 2025/6/24 12:14
 */
public class BookSpecification {
    private String title;      // 书名
    private String author;     // 作者
    private Double minPrice;   // 最低价格
    private Double maxPrice;   // 最高价格
    private Book.BookStatus status;  // 图书状态
    private Set<Long> categoryIds;  // 新增：分类ID集合


    /**
     * 默认构造函数
     */
    public BookSpecification() {}

    /**
     * 带参构造函数
     *
     * @param title     书名
     * @param author    作者
     * @param minPrice  最低价格
     * @param maxPrice  最高价格
     * @param status    图书状态
     */
    public BookSpecification(String title, String author, Double minPrice, Double maxPrice, Book.BookStatus status) {
        this.title = title;
        this.author = author;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
        this.status = status;
    }

    /**
     * 获取书名查询条件
     *
     * @return 书名
     */
    public String getTitle() {
        return title;
    }

    /**
     * 设置书名查询条件
     *
     * @param title 书名
     */
    public void setTitle(String title) {
        this.title = title;
    }

    /**
     * 获取作者查询条件
     *
     * @return 作者
     */
    public String getAuthor() {
        return author;
    }

    /**
     * 设置作者查询条件
     *
     * @param author 作者
     */
    public void setAuthor(String author) {
        this.author = author;
    }

    /**
     * 获取最低价格查询条件
     *
     * @return 最低价格
     */
    public Double getMinPrice() {
        return minPrice;
    }

    /**
     * 设置最低价格查询条件
     *
     * @param minPrice 最低价格
     */
    public void setMinPrice(Double minPrice) {
        this.minPrice = minPrice;
    }

    /**
     * 获取最高价格查询条件
     *
     * @return 最高价格
     */
    public Double getMaxPrice() {
        return maxPrice;
    }

    /**
     * 设置最高价格查询条件
     *
     * @param maxPrice 最高价格
     */
    public void setMaxPrice(Double maxPrice) {
        this.maxPrice = maxPrice;
    }

    /**
     * 获取图书状态查询条件
     *
     * @return 图书状态
     */
    public Book.BookStatus getStatus() {
        return status;
    }

    /**
     * 设置图书状态查询条件
     *
     * @param status 图书状态
     */
    public void setStatus(Book.BookStatus status) {
        this.status = status;
    }

    /**
     * 构建JPA Specification对象，用于动态查询
     *
     * @return Specification对象，包含所有非空查询条件的组合
     */
    public Specification<Book> getSpecification() {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 动态添加书名条件（模糊查询，忽略大小写）
            Optional.ofNullable(title)
                    .ifPresent(t -> predicates.add(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("title")), "%" + t.toLowerCase() + "%")));

            // 动态添加作者条件（模糊查询，忽略大小写）
            Optional.ofNullable(author)
                    .ifPresent(a -> predicates.add(
                            criteriaBuilder.like(criteriaBuilder.lower(root.get("author")), "%" + a.toLowerCase() + "%")));

            // 动态添加价格范围条件
            if (minPrice != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("price"), BigDecimal.valueOf(minPrice)));
            }
            if (maxPrice != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("price"), BigDecimal.valueOf(maxPrice)));
            }

            // 动态添加状态条件
            Optional.ofNullable(status)
                    .ifPresent(s -> predicates.add(criteriaBuilder.equal(root.get("status"), s)));

            // 将所有条件用AND连接
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public Set<Long> getCategoryIds() { return categoryIds; }
    public void setCategoryIds(Set<Long> categoryIds) { this.categoryIds = categoryIds; }
}
