package com.raynnie.bookcatalog.repository;

import com.raynnie.bookcatalog.domain.Book;
import com.raynnie.bookcatalog.domain.Category;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 12:08
 *
 * 图书仓库接口，继承JpaRepository提供基本CRUD功能
 * 并定义自定义查询方法
 */
@Repository
public interface BookRepository extends JpaRepository<Book, Long>, JpaSpecificationExecutor<Book> {
    /**
     * 检查ISBN是否存在
     */
    boolean existsByIsbn(String isbn);

    Optional<Book> findByIsbn(String isbn);

    /**
     * 根据书名或作者模糊查询
     */
    List<Book> findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(String title, String author);

    /**
     * 根据分类查询图书
     */
    List<Book> findByCategoriesContaining(Category category);

    /**
     * 根据状态查询图书
     */
    List<Book> findByStatus(Book.BookStatus status);

    /**
     * 根据价格范围查询图书
     */
    @Query("SELECT b FROM Book b WHERE b.price BETWEEN :minPrice AND :maxPrice")
    List<Book> findByPriceRange(Double minPrice, Double maxPrice);

    /**
     * 分页查询所有图书
     */
    @Override
    Page<Book> findAll(Pageable pageable);

    /**
     * 根据作者名称查询图书
     * @param author
     * @return
     */
    List<Book> findByAuthorContainingIgnoreCase(String author);
}
