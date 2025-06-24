package com.raynnie.bookcatalog.service.impl;

import com.raynnie.bookcatalog.domain.Book;
import com.raynnie.bookcatalog.domain.Category;
import com.raynnie.bookcatalog.dto.BookRequestDto;
import com.raynnie.bookcatalog.exception.BookCatalogBusinessException;
import com.raynnie.bookcatalog.exception.BookCatalogErrorCode;
import com.raynnie.bookcatalog.repository.BookRepository;
import com.raynnie.bookcatalog.repository.BookSpecification;
import com.raynnie.bookcatalog.repository.CategoryRepository;
import com.raynnie.bookcatalog.service.BookService;

import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 图书服务实现类，实现图书管理的具体业务逻辑
 * 应用工厂模式创建图书实例
 * @Author Raynnie.J
 * @Date 2025/6/24 12:18
 */
@Service
@Transactional
public class BookServiceImpl implements BookService {
    private final BookRepository bookRepository;
    private final BookFactory bookFactory;
    private final CategoryRepository categoryRepository;

    /**
     * 构造函数，注入依赖组件
     *
     * @param bookRepository 图书仓库
     * @param categoryRepository 分类仓库
     * @param bookFactory    图书工厂
     */
    public BookServiceImpl(BookRepository bookRepository, CategoryRepository categoryRepository, BookFactory bookFactory) {
        this.bookRepository = bookRepository;
        this.categoryRepository = categoryRepository;
        this.bookFactory = bookFactory;
    }

    @Override
    public Book createBook(BookRequestDto requestDto) {
        Book book = convertDTOtoEntity(requestDto);
        // 检查ISBN是否已存在
        if (bookRepository.existsByIsbn(book.getIsbn())) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.BOOK_ALREADY_EXISTS.getCode(),
                    "图书ISBN已存在，无法创建重复图书"
            );
        }

        // 使用工厂创建图书实例，确保初始化逻辑统一
        Book newBook = bookFactory.createBook(
                book.getTitle(),
                book.getAuthor(),
                book.getPrice(),
                book.getPublishDate(),
                book.getIsbn()
        );
        // 设置默认状态
        newBook.setStatus(Book.BookStatus.AVAILABLE);
        return bookRepository.save(newBook);
    }

    @Override
    public Optional<Book> getBookById(Long id) {
        return bookRepository.findById(id);
    }

    @Override
    public List<Book> getAllBooks() {
        return bookRepository.findAll();
    }

    @Override
    public Page<Book> getAllBooksPaginated(Pageable pageable) {
        return bookRepository.findAll(pageable);
    }

    private Book convertDTOtoEntity(BookRequestDto requestDto) {
        Book book = new Book();
        book.setTitle(requestDto.getTitle());
        book.setAuthor(requestDto.getAuthor());
        book.setPrice(requestDto.getPrice());
        book.setPublishDate(requestDto.getPublishDate());
        book.setIsbn(requestDto.getIsbn());
        return book;
    }

    @Override
    public Book updateBook(Long id, BookRequestDto request) {
        Book bookDetails = createBook(request);
        // 1. 检查图书是否存在
        Book existingBook = bookRepository.findById(id)
                .orElseThrow(() -> new BookCatalogBusinessException(
                        BookCatalogErrorCode.BOOK_NOT_FOUND.getCode(),
                        "图书不存在，ID: " + id
                ));

        // 2. 更新基本属性
        updateBasicProperties(existingBook, bookDetails);

        // 3. 验证并更新ISBN（如果有变化）
        if (bookDetails.getIsbn() != null && !bookDetails.getIsbn().equals(existingBook.getIsbn())) {
            validateIsbnUniqueness(bookDetails.getIsbn());
            existingBook.setIsbn(bookDetails.getIsbn());
        }

        // 4. 验证并更新状态（如果有变化）
        if (bookDetails.getStatus() != null && bookDetails.getStatus() != existingBook.getStatus()) {
            validateStatusTransition(existingBook, bookDetails.getStatus());
            existingBook.setStatus(bookDetails.getStatus());
        }

        // 5. 更新分类（如果有变化）
        if (bookDetails.getCategories() != null && !bookDetails.getCategories().isEmpty()) {
            updateCategories(existingBook, bookDetails.getCategories());
        }

        // 6. 保存并返回更新后的图书
        return bookRepository.save(existingBook);
    }

    /**
     * 更新图书基本属性
     */
    private void updateBasicProperties(Book existingBook, Book bookDetails) {
        if (bookDetails.getTitle() != null) existingBook.setTitle(bookDetails.getTitle());
        if (bookDetails.getAuthor() != null) existingBook.setAuthor(bookDetails.getAuthor());
        if (bookDetails.getPrice() != null) existingBook.setPrice(bookDetails.getPrice());
        if (bookDetails.getPublishDate() != null) existingBook.setPublishDate(bookDetails.getPublishDate());
    }

    /**
     * 验证ISBN唯一性
     */
    private void validateIsbnUniqueness(String isbn) {
        if (bookRepository.existsByIsbn(isbn)) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.BOOK_ALREADY_EXISTS.getCode(),
                    "新ISBN已被其他图书使用: " + isbn
            );
        }
    }

    /**
     * 验证状态转换合法性
     */
    private void validateStatusTransition(Book book, Book.BookStatus newStatus) {
        if (book.getStatus() == Book.BookStatus.DISCONTINUED &&
                newStatus == Book.BookStatus.AVAILABLE) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.BOOK_STATUS_INVALID.getCode(),
                    "已下架图书不能恢复为可借阅状态"
            );
        }
    }

    /**
     * 更新图书分类
     */
    private void updateCategories(Book existingBook, Set<Category> newCategories) {
        // 验证所有分类是否存在
        Set<Long> categoryIds = newCategories.stream()
                .map(Category::getId)
                .collect(Collectors.toSet());

        List<Category> managedCategories = categoryRepository.findAllById(categoryIds);

        if (managedCategories.size() != categoryIds.size()) {
            Set<Long> existingIds = managedCategories.stream()
                    .map(Category::getId)
                    .collect(Collectors.toSet());

            categoryIds.removeAll(existingIds);
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.CATEGORY_NOT_FOUND.getCode(),
                    "分类不存在，ID: " + categoryIds
            );
        }

        // 更新分类关联
        existingBook.getCategories().clear();
        existingBook.getCategories().addAll(managedCategories);
    }

    @Override
    public void deleteBook(Long categoryId) {
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BookCatalogBusinessException(
                        BookCatalogErrorCode.CATEGORY_NOT_FOUND.getCode(),
                        "分类不存在，ID: " + categoryId
                ));

        // 检查分类是否包含图书
        if (!category.getBooks().isEmpty()) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.CATEGORY_HAS_ASSOCIATED_BOOKS.getCode(),
                    "分类包含 " + category.getBooks().size() + " 本图书，无法删除"
            );
        }
    }

    @Override
    public List<Book> searchBooks(BookSpecification specification) {
        // 1. 构建动态查询条件
        Specification<Book> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // 添加书名模糊查询条件
            if (specification.getTitle() != null && !specification.getTitle().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("title")),
                        "%" + specification.getTitle().toLowerCase() + "%"
                ));
            }

            // 添加作者模糊查询条件
            if (specification.getAuthor() != null && !specification.getAuthor().isEmpty()) {
                predicates.add(criteriaBuilder.like(
                        criteriaBuilder.lower(root.get("author")),
                        "%" + specification.getAuthor().toLowerCase() + "%"
                ));
            }

            // 添加价格范围条件
            if (specification.getMinPrice() != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(
                        root.get("price"),
                        BigDecimal.valueOf(specification.getMinPrice())
                ));
            }

            if (specification.getMaxPrice() != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(
                        root.get("price"),
                        BigDecimal.valueOf(specification.getMaxPrice())
                ));
            }

            // 添加状态条件
            if (specification.getStatus() != null) {
                predicates.add(criteriaBuilder.equal(
                        root.get("status"),
                        specification.getStatus()
                ));
            }

            // 添加分类条件（如果有分类ID）
            if (specification.getCategoryIds() != null && !specification.getCategoryIds().isEmpty()) {
                Join<Book, Category> categoryJoin = root.join("categories", JoinType.INNER);
                predicates.add(categoryJoin.get("id").in(specification.getCategoryIds()));
            }

            // 将所有条件用AND连接
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // 2. 执行查询并返回结果
        return bookRepository.findAll(spec);
    }

    @Override
    public List<Book> getBooksByAuthor(String author) {
        if (author == null || author.trim().isEmpty()) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.INVALID_PARAMETER.getCode(),
                    "作者名称不能为空"
            );
        }

        // 使用Repository的模糊查询方法
        return bookRepository.findByAuthorContainingIgnoreCase(author.trim());
    }

    @Override
    public List<Book> getAvailableBooks() {
        // 直接调用Repository的状态查询方法
        return bookRepository.findByStatus(Book.BookStatus.AVAILABLE);
    }

    @Override
    public List<Book> searchByTitleOrAuthor(String title, String author) {
        // 处理空值，避免SQL注入风险
        title = StringUtils.defaultString(title);
        author = StringUtils.defaultString(author);

        // 调用Repository方法执行模糊查询
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(title, author);
    }

    @Override
    public List<Book> getBooksByCategory(Long categoryId) {
        // 验证分类是否存在
        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new BookCatalogBusinessException(
                        BookCatalogErrorCode.CATEGORY_NOT_FOUND.getCode(),
                        "分类不存在，ID: " + categoryId
                ));

        // 调用Repository方法查询关联图书
        return bookRepository.findByCategoriesContaining(category);
    }

    @Override
    public List<Book> getBooksByPriceRange(Double minPrice, Double maxPrice) {
        // 参数校验
        if (minPrice == null || maxPrice == null) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.INVALID_PARAMETER.getCode(),
                    "价格范围参数不能为空"
            );
        }

        if (minPrice > maxPrice) {
            throw new BookCatalogBusinessException(
                    BookCatalogErrorCode.INVALID_PARAMETER.getCode(),
                    "最低价格不能大于最高价格"
            );
        }

        // 调用Repository方法执行价格范围查询
        return bookRepository.findByPriceRange(minPrice, maxPrice);
    }
}
