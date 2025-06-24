package com.raynnie.bookcatalog.service;

import com.raynnie.bookcatalog.domain.Book;
import com.raynnie.bookcatalog.domain.Category;
import com.raynnie.bookcatalog.dto.BookRequestDto;
import com.raynnie.bookcatalog.exception.BookCatalogBusinessException;
import com.raynnie.bookcatalog.exception.BookCatalogErrorCode;
import com.raynnie.bookcatalog.repository.BookRepository;
import com.raynnie.bookcatalog.repository.CategoryRepository;
import com.raynnie.bookcatalog.service.impl.BookFactory;
import com.raynnie.bookcatalog.service.impl.BookServiceImpl;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collections;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 16:06
 */

@ExtendWith(MockitoExtension.class)
class BookServiceImplTest {

    @Mock
    private BookRepository bookRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @Mock
    private BookFactory bookFactory;

    @InjectMocks
    private BookServiceImpl bookService;

    // 创建图书测试
    @Test
    void createBook_WithUniqueIsbn_ReturnsCreatedBookWithAvailableStatus() {
        // Arrange
        String uniqueIsbn = "978-3-16-148410-0";
        BookRequestDto requestDto = BookRequestDto.builder()
                .title("Clean Code")
                .author("Robert C. Martin")
                .price(BigDecimal.valueOf(45.99))
                .publishDate(LocalDate.now())
                .isbn(uniqueIsbn)
                .build();

        Book mockBook = Book.buildBook()
                .title(requestDto.getTitle())
                .author(requestDto.getAuthor())
                .price(requestDto.getPrice())
                .publishDate(requestDto.getPublishDate())
                .isbn(requestDto.getIsbn())
                .status(Book.BookStatus.AVAILABLE)
                .build();

        // Mock repository behavior
        when(bookRepository.existsByIsbn(uniqueIsbn)).thenReturn(false);
        when(bookFactory.createBook(
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getPrice(),
                requestDto.getPublishDate(),
                requestDto.getIsbn()
        )).thenReturn(mockBook);
        when(bookRepository.save(any(Book.class))).thenReturn(mockBook);

        // Act
        Book result = bookService.createBook(requestDto);

        // Assert
        assertNotNull(result);
        assertEquals(requestDto.getTitle(), result.getTitle());
        assertEquals(requestDto.getAuthor(), result.getAuthor());
        assertEquals(requestDto.getIsbn(), result.getIsbn());
        assertEquals(Book.BookStatus.AVAILABLE, result.getStatus());

        // Verify interactions
        verify(bookRepository, times(1)).existsByIsbn(uniqueIsbn);
        verify(bookFactory, times(1)).createBook(
                requestDto.getTitle(),
                requestDto.getAuthor(),
                requestDto.getPrice(),
                requestDto.getPublishDate(),
                requestDto.getIsbn()
        );
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void createBook_WithDuplicateIsbn_ThrowsBookAlreadyExistsException() {
        // Arrange
        String duplicateIsbn = "978-3-16-148410-0";
        BookRequestDto requestDto = BookRequestDto.builder()
                .title("Duplicate Book")
                .author("Test Author")
                .price(BigDecimal.valueOf(29.99))
                .publishDate(LocalDate.now())
                .isbn(duplicateIsbn)
                .build();

        // Mock repository to return true for existsByIsbn check
        when(bookRepository.existsByIsbn(duplicateIsbn)).thenReturn(true);

        // Act & Assert
        BookCatalogBusinessException exception = assertThrows(BookCatalogBusinessException.class,
                () -> bookService.createBook(requestDto));

        assertEquals("BOOK_ALREADY_EXISTS", exception.getErrorCode());
        assertEquals("图书ISBN已存在，无法创建重复图书", exception.getMessage());

        // Verify interactions
        verify(bookRepository, times(1)).existsByIsbn(duplicateIsbn);
        verify(bookFactory, never()).createBook(any(), any(), any(), any(), any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void getBookById_ExistingId_ReturnsBook() {
        // Arrange
        Long existingBookId = 1L;
        Book mockBook = Book.buildBook().id(existingBookId).build();

        when(bookRepository.findById(existingBookId)).thenReturn(Optional.of(mockBook));

        // Act
        Optional<Book> result = bookService.getBookById(existingBookId);

        // Assert
        assertNotNull(result);
        Book book = result.get();
        assertEquals(existingBookId, book.getId());
        verify(bookRepository, times(1)).findById(existingBookId);
    }

    @Test
    void getBookById_NonExistentId_ThrowsException() {
        // Arrange
        Long nonExistentId = 999L;
        when(bookRepository.findById(nonExistentId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(BookCatalogBusinessException.class, () -> {
            bookService.getBookById(nonExistentId);
        });

        // Verify
        verify(bookRepository, times(1)).findById(nonExistentId);
    }

    @Test
    void updateBook_WithValidChanges_ReturnsUpdatedBook() {
        // Arrange
        Long bookId = 1L;
        Category existingCategory = new Category();
        existingCategory.setId(1L);

        BookRequestDto.CategoryDto categoryDto = BookRequestDto.CategoryDto.builder()
                .id(1L)
                .build();

        Book existingBook = Book.buildBook()
                .id(bookId)
                .title("Old Title")
                .author("Old Author")
                .price(BigDecimal.valueOf(29.99))
                .publishDate(LocalDate.of(2020, 1, 1))
                .isbn("old-isbn-123")
                .status(Book.BookStatus.AVAILABLE)
                .categories(Collections.singleton(existingCategory))
                .build();

        BookRequestDto updateRequest = BookRequestDto.builder()
                .title("New Title")
                .author("New Author")
                .price(BigDecimal.valueOf(39.99))
                .publishDate(LocalDate.of(2023, 1, 1))
                .isbn("new-isbn-456")
                .categories(Set.of(categoryDto))
                .build();

        // Mock repository behavior
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbn("new-isbn-456")).thenReturn(false);
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(existingCategory));
        when(bookRepository.save(any(Book.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // Act
        Book updatedBook = bookService.updateBook(bookId, updateRequest);

        // Assert
        assertNotNull(updatedBook);
        assertEquals(bookId, updatedBook.getId());
        assertEquals("New Title", updatedBook.getTitle());
        assertEquals("New Author", updatedBook.getAuthor());
        assertEquals(BigDecimal.valueOf(39.99), updatedBook.getPrice());
        assertEquals(LocalDate.of(2023, 1, 1), updatedBook.getPublishDate());
        assertEquals("new-isbn-456", updatedBook.getIsbn());
        assertEquals(Book.BookStatus.AVAILABLE, updatedBook.getStatus());
        assertTrue(updatedBook.getCategories().contains(existingCategory));

        // Verify interactions
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).existsByIsbn("new-isbn-456");
        verify(categoryRepository, times(1)).findById(1L);
        verify(bookRepository, times(1)).save(any(Book.class));
    }

    @Test
    void updateBook_NonExistentBook_ThrowsNotFoundException() {
        // Arrange
        Long nonExistentBookId = 999L;
        BookRequestDto requestDto = BookRequestDto.builder()
                .title("New Title")
                .build();

        when(bookRepository.findById(nonExistentBookId)).thenReturn(Optional.empty());

        // Act & Assert
        BookCatalogBusinessException exception = assertThrows(BookCatalogBusinessException.class, () -> {
            bookService.updateBook(nonExistentBookId, requestDto);
        });

        assertEquals("BOOK_NOT_FOUND", exception.getErrorCode());

        // Verify interactions
        verify(bookRepository, times(1)).findById(nonExistentBookId);
        verify(bookRepository, never()).existsByIsbn(any());
        verify(bookRepository, never()).save(any());
    }

    @Test
    void updateBook_WithDuplicateIsbn_ThrowsException() {
        // Arrange
        Long bookId = 1L;
        String existingIsbn = "978-3-16-148410-0";

        Book existingBook = Book.buildBook()
                .id(bookId)
                .title("Existing Book")
                .isbn("original-isbn")
                .status(Book.BookStatus.AVAILABLE)
                .build();

        BookRequestDto requestDto = BookRequestDto.builder()
                .title("Updated Title")
                .isbn(existingIsbn)
                .build();

        // Mock repository behavior
        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));
        when(bookRepository.existsByIsbn(existingIsbn)).thenReturn(true);

        // Act & Assert
        BookCatalogBusinessException exception = assertThrows(BookCatalogBusinessException.class, () -> {
            bookService.updateBook(bookId, requestDto);
        });

        assertEquals(BookCatalogErrorCode.BOOK_ALREADY_EXISTS.getCode(), exception.getErrorCode());
        assertEquals("图书ISBN已存在，无法创建重复图书", exception.getMessage());

        // Verify interactions
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, times(1)).existsByIsbn(existingIsbn);
        verify(bookRepository, never()).save(any(Book.class));
    }

    @Test
    void updateBook_StatusFromDiscontinuedToAvailable_ThrowsExceptionWithInvalidStatusErrorCode() {
        // Arrange
        Long bookId = 1L;
        Book existingBook = Book.buildBook()
                .id(bookId)
                .status(Book.BookStatus.DISCONTINUED)
                .build();

        BookRequestDto requestDto = BookRequestDto.builder()
                .status(Book.BookStatus.AVAILABLE)
                .build();

        when(bookRepository.findById(bookId)).thenReturn(Optional.of(existingBook));

        // Act & Assert
        BookCatalogBusinessException exception = assertThrows(BookCatalogBusinessException.class, () -> {
            bookService.updateBook(bookId, requestDto);
        });

        assertEquals(BookCatalogErrorCode.BOOK_STATUS_INVALID.getCode(), exception.getErrorCode());
        verify(bookRepository, times(1)).findById(bookId);
        verify(bookRepository, never()).save(any());
    }
}
