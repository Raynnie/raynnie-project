package com.raynnie.bookcatalog.dto;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 15:17
 */
import com.raynnie.bookcatalog.domain.Book;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Set;

import lombok.Builder;
import lombok.Data;

@Data
@Builder
public class BookRequestDto {

    @NotBlank(message = "书名不能为空")
    private String title;

    @NotBlank(message = "作者不能为空")
    private String author;

    @NotNull(message = "价格不能为空")
    private BigDecimal price;

    private LocalDate publishDate;

    @NotBlank(message = "ISBN不能为空")
    private String isbn;

    private Set<CategoryDto> categories;

    @Enumerated(EnumType.STRING)
    private Book.BookStatus status;

    @Data
    @Builder
    public static class CategoryDto {
        private Long id;
        private String name;
    }
}