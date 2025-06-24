package com.raynnie.bookcatalog.dto;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 15:17
 */
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import com.raynnie.bookcatalog.domain.Book;
import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookResponseDto {
    private Long id;
    private String title;
    private String author;
    private BigDecimal price;
    private LocalDate publishDate;
    private String isbn;
    private Book.BookStatus status;
    private Set<CategoryDto> categories;

    @Data
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;

        // 改为 public 构造方法
        public CategoryDto(Long id, String name) {
            this.id = id;
            this.name = name;
        }
    }
}
