package com.raynnie.bookcatalog.service;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 17:39
 */

import com.fasterxml.jackson.databind.ObjectMapper;
import com.raynnie.bookcatalog.domain.Book;
import com.raynnie.bookcatalog.domain.Category;
import com.raynnie.bookcatalog.dto.BookRequestDto;
import com.raynnie.bookcatalog.repository.BookRepository;
import com.raynnie.bookcatalog.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class BookControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private BookRepository bookRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private Category testCategory;

    @BeforeEach
    void setUp() {
        // 初始化测试数据
        testCategory = new Category();
        testCategory.setName("计算机");
        testCategory = categoryRepository.save(testCategory);
    }

    // 创建图书测试
    @Test
    void createBook_ValidInput_ReturnsCreatedBook() throws Exception {
        // 准备请求DTO
        BookRequestDto request = BookRequestDto.builder()
                .title("Spring实战")
                .author("Craig Walls")
                .price(BigDecimal.valueOf(89.99))
                .publishDate(LocalDate.now())
                .isbn("978-7-121-37213-0")
                .categories(Set.of(
                        BookRequestDto.CategoryDto.builder()
                                .id(testCategory.getId())
                                .name(testCategory.getName())
                                .build()
                ))
                .build();

        // 执行HTTP请求
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.title").value("Spring实战"))
                .andExpect(jsonPath("$.status").value("AVAILABLE"))
                .andExpect(jsonPath("$.categories[0].name").value("计算机"));

        // 验证数据库中是否存在该图书
        Optional<Book> savedBook = bookRepository.findByIsbn("978-7-121-37213-0");
        assertTrue(savedBook.isPresent());
        assertEquals("Spring实战", savedBook.get().getTitle());
    }

    @Test
    void createBook_DuplicateIsbn_ReturnsConflict() throws Exception {
        // 先创建一本具有相同ISBN的图书
        Book existingBook = new Book();
        existingBook.setIsbn("duplicate-isbn");
        bookRepository.save(existingBook);

        // 准备重复ISBN的请求
        BookRequestDto request = BookRequestDto.builder()
                .isbn("duplicate-isbn")
                .build();

        // 执行HTTP请求
        mockMvc.perform(post("/api/v1/books")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value("ISBN已存在"));
    }

    // 获取图书测试
    @Test
    void getBookById_ExistingId_ReturnsBook() throws Exception {
        // 准备测试数据
        Book book = new Book();
        book.setTitle("测试图书");
        book.setIsbn("test-isbn");
        book.setStatus(Book.BookStatus.AVAILABLE);
        book.setCategories(Collections.singleton(testCategory));
        book = bookRepository.save(book);

        // 执行HTTP请求
        mockMvc.perform(get("/api/v1/books/{id}", book.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("测试图书"))
                .andExpect(jsonPath("$.categories[0].name").value("计算机"));
    }

    @Test
    void getBookById_NonExistingId_ReturnsNotFound() throws Exception {
        // 执行HTTP请求（不存在的ID）
        mockMvc.perform(get("/api/v1/books/{id}", 999L))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("图书不存在"));
    }

    // 更新图书测试
    @Test
    void updateBook_ValidInput_ReturnsUpdatedBook() throws Exception {
        // 准备测试数据
        Book book = new Book();
        book.setTitle("旧书名");
        book.setIsbn("old-isbn");
        book.setStatus(Book.BookStatus.AVAILABLE);
        book.setCategories(Collections.singleton(testCategory));
        book = bookRepository.save(book);

        // 准备更新请求
        BookRequestDto request = BookRequestDto.builder()
                .title("新书名")
                .isbn("new-isbn")
                .status(Book.BookStatus.UNAVAILABLE)
                .categories(Set.of(
                        BookRequestDto.CategoryDto.builder()
                                .id(testCategory.getId())
                                .build()
                ))
                .build();

        // 执行HTTP请求
        mockMvc.perform(put("/api/v1/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.title").value("新书名"))
                .andExpect(jsonPath("$.isbn").value("new-isbn"))
                .andExpect(jsonPath("$.status").value("BORROWED"));
    }

    @Test
    void updateBook_InvalidStatusTransition_ReturnsBadRequest() throws Exception {
        // 准备测试数据（已停用的图书）
        Book book = new Book();
        book.setStatus(Book.BookStatus.DISCONTINUED);
        book = bookRepository.save(book);

        // 准备非法状态转换请求
        BookRequestDto request = BookRequestDto.builder()
                .status(Book.BookStatus.AVAILABLE)
                .build();

        // 执行HTTP请求
        mockMvc.perform(put("/api/v1/books/{id}", book.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("无法从DISCONTINUED状态转换到AVAILABLE"));
    }

    // 删除图书测试
    @Test
    void deleteBook_ExistingId_ReturnsNoContent() throws Exception {
        // 准备测试数据
        Book book = new Book();
        book.setIsbn("delete-isbn");
        book = bookRepository.save(book);

        // 执行HTTP请求
        mockMvc.perform(delete("/api/v1/books/{id}", book.getId()))
                .andExpect(status().isNoContent());

        // 验证图书已被删除
        assertFalse(bookRepository.existsById(book.getId()));
    }

    @Test
    void deleteBook_NonExistingId_ReturnsNotFound() throws Exception {
        // 执行HTTP请求（不存在的ID）
        mockMvc.perform(delete("/api/v1/books/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    // 搜索图书测试
    @Test
    void searchBooks_ValidKeyword_ReturnsBooks() throws Exception {
        // 准备测试数据
        Book book1 = new Book();
        book1.setTitle("Java编程思想");
        bookRepository.save(book1);

        Book book2 = new Book();
        book2.setAuthor("王");
        bookRepository.save(book2);

        // 执行HTTP请求
        mockMvc.perform(get("/api/v1/books/search")
                        .param("title", "Java")
                        .param("author", "王"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(2));
    }

    // 获取可借阅图书测试
    @Test
    void getAvailableBooks_ReturnsAvailableBooks() throws Exception {
        // 准备测试数据
        Book availableBook = new Book();
        availableBook.setStatus(Book.BookStatus.AVAILABLE);
        bookRepository.save(availableBook);

        Book borrowedBook = new Book();
        borrowedBook.setStatus(Book.BookStatus.UNAVAILABLE);
        bookRepository.save(borrowedBook);

        // 执行HTTP请求
        mockMvc.perform(get("/api/v1/books/available"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // 按分类获取图书测试
    @Test
    void getBooksByCategory_ExistingCategory_ReturnsBooks() throws Exception {
        // 准备测试数据
        Book book = new Book();
        book.setCategories(Collections.singleton(testCategory));
        bookRepository.save(book);

        // 执行HTTP请求
        mockMvc.perform(get("/api/v1/books/category/{categoryId}", testCategory.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // 按价格范围获取图书测试
    @Test
    void getBooksByPriceRange_ValidRange_ReturnsBooks() throws Exception {
        // 准备测试数据
        Book book = new Book();
        book.setPrice(BigDecimal.valueOf(50));
        bookRepository.save(book);

        // 执行HTTP请求
        mockMvc.perform(get("/api/v1/books/price-range")
                        .param("minPrice", "40")
                        .param("maxPrice", "60"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.size()").value(1));
    }

    // 分页获取图书测试
    @Test
    void getAllBooksPaginated_ReturnsPaginatedBooks() throws Exception {
        // 准备测试数据
        Book book1 = new Book();
        bookRepository.save(book1);

        Book book2 = new Book();
        bookRepository.save(book2);

        // 执行HTTP请求（第一页，每页1条记录）
        mockMvc.perform(get("/api/v1/books")
                        .param("page", "0")
                        .param("size", "1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.size()").value(1))
                .andExpect(jsonPath("$.totalElements").value(2));
    }
}
