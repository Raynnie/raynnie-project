package com.raynnie.bookcatalog.controller;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 15:16
 */
import com.raynnie.bookcatalog.domain.Book;
import com.raynnie.bookcatalog.dto.BookRequestDto;
import com.raynnie.bookcatalog.dto.BookResponseDto;
import com.raynnie.bookcatalog.exception.BookCatalogBusinessException;
import com.raynnie.bookcatalog.service.BookService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/books")
@Tag(name = "图书管理", description = "图书信息管理API")
public class BookController {

    private final BookService bookService;

    public BookController(BookService bookService) {
        this.bookService = bookService;
    }

    // 创建图书
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(summary = "创建图书", description = "根据提供的信息创建新图书")
    public BookResponseDto createBook(@RequestBody BookRequestDto request) {
        Book book = bookService.createBook(request);
        return mapToDto(book);
    }

    // 获取所有图书（分页）
    @GetMapping
    @Operation(summary = "获取图书列表", description = "分页获取图书列表，支持排序")
    public Page<BookResponseDto> getAllBooks(Pageable pageable) {
        return bookService.getAllBooksPaginated(pageable)
                .map(this::mapToDto);
    }

    // 根据ID获取图书
    @GetMapping("/{id}")
    @Operation(summary = "获取图书详情", description = "根据图书ID获取详细信息")
    public ResponseEntity<BookResponseDto> getBookById(@PathVariable Long id) {
        return bookService.getBookById(id)
                .map(book -> ResponseEntity.ok(mapToDto(book)))
                .orElseThrow(() -> new BookCatalogBusinessException(
                        "BOOK_NOT_FOUND", "图书不存在，ID: " + id));
    }

    // 根据ID更新图书
    @PutMapping("/{id}")
    @Operation(summary = "更新图书信息", description = "根据图书ID更新图书的基本信息")
    public BookResponseDto updateBook(@PathVariable Long id, @RequestBody BookRequestDto request) {
        Book updatedBook = bookService.updateBook(id, request);
        return mapToDto(updatedBook);
    }

    // 根据ID删除图书
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @Operation(summary = "删除图书", description = "根据图书ID删除图书")
    public void deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
    }

    // 根据书名或作者搜索图书
    @GetMapping("/search")
    @Operation(summary = "搜索图书", description = "根据书名或作者模糊搜索图书")
    public List<BookResponseDto> searchBooks(
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String author) {
        return bookService.searchByTitleOrAuthor(title, author)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 获取可借阅的图书
    @GetMapping("/available")
    @Operation(summary = "获取可借阅图书", description = "获取状态为可借阅的图书列表")
    public List<BookResponseDto> getAvailableBooks() {
        return bookService.getAvailableBooks()
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 根据分类获取图书
    @GetMapping("/category/{categoryId}")
    @Operation(summary = "根据分类获取图书", description = "获取指定分类下的所有图书")
    public List<BookResponseDto> getBooksByCategory(@PathVariable Long categoryId) {
        return bookService.getBooksByCategory(categoryId)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 根据价格范围获取图书
    @GetMapping("/price-range")
    @Operation(summary = "根据价格范围获取图书", description = "获取指定价格区间内的图书")
    public List<BookResponseDto> getBooksByPriceRange(
            @RequestParam Double minPrice,
            @RequestParam Double maxPrice) {
        return bookService.getBooksByPriceRange(minPrice, maxPrice)
                .stream()
                .map(this::mapToDto)
                .collect(Collectors.toList());
    }

    // 实体到DTO的映射方法
    public BookResponseDto mapToDto(Book book) {
        // 安全地处理 categories 集合
        Set<BookResponseDto.CategoryDto> categoryDtos = book.getCategories() != null
                ? book.getCategories().stream()
                .map(category -> new BookResponseDto.CategoryDto(
                        category.getId(),
                        category.getName()
                ))
                .collect(Collectors.toSet())
                : Collections.emptySet();

        return BookResponseDto.builder()
                .id(book.getId())
                .title(book.getTitle())
                .author(book.getAuthor())
                .categories(categoryDtos)
                .build();
    }
}
