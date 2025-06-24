package com.raynnie.bookcatalog.service;

import com.raynnie.bookcatalog.domain.Book;
import com.raynnie.bookcatalog.dto.BookRequestDto;
import com.raynnie.bookcatalog.repository.BookSpecification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
/**
 * 图书服务接口，定义图书管理的业务方法
 * @Author Raynnie.J
 * @Date 2025/6/24 12:16
 */
public interface BookService {
    /**
     * 创建新图书
     * @param requestDto 请求DTO
     * @return 创建成功的图书实体
     */
    Book createBook(BookRequestDto requestDto);

    /**
     * 根据ID获取图书
     * @param id 图书ID
     * @return 包含图书的Optional对象，若不存在则返回空Optional
     */
    Optional<Book> getBookById(Long id);

    /**
     * 获取所有图书
     *
     * @return 图书列表
     */
    List<Book> getAllBooks();

    /**
     * 分页获取图书
     * @param pageable 分页参数
     * @return 分页图书数据
     */
    Page<Book> getAllBooksPaginated(Pageable pageable);

    /**
     * 更新图书信息
     * @param id          图书ID
     * @param request 包含更新信息的图书DTO
     * @return 更新后的图书实体
     */
    Book updateBook(Long id, BookRequestDto request);

    /**
     * 删除图书
     * @param id 图书ID
     */
    void deleteBook(Long id);

    /**
     * 根据多条件搜索图书
     * @param specification 包含查询条件的Specification对象
     * @return 匹配的图书列表
     */
    List<Book> searchBooks(BookSpecification specification);

    /**
     * 根据作者查询图书
     * @param author 作者姓名
     * @return 匹配的图书列表
     */
    List<Book> getBooksByAuthor(String author);

    /**
     * 获取可借阅的图书
     * @return 状态为AVAILABLE的图书列表
     */
    List<Book> getAvailableBooks();

    /**
     * 根据图书名称或作者名获取图书
     * @param title
     * @param author
     * @return
     */
    List<Book> searchByTitleOrAuthor(String title, String author);

    /**
     * 根据分类id获取图书
     * @param categoryId
     * @return
     */
    List<Book> getBooksByCategory(Long categoryId);

    /**
     * 根据价格区间获取图书
     * @param minPrice
     * @param maxPrice
     * @return
     */
    List<Book> getBooksByPriceRange(Double minPrice, Double maxPrice);
}
