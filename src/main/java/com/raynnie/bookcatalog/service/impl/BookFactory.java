package com.raynnie.bookcatalog.service.impl;

import com.raynnie.bookcatalog.domain.Book;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 12:21
 * 图书工厂 - 应用工厂模式
 */
@Service
public class BookFactory {

    public Book createBook(String title, String author, BigDecimal price,
                           LocalDate publishDate, String isbn) {
        return Book.buildBook() //建造者模式
                .title(title)
                .author(author)
                .price(price)
                .publishDate(publishDate)
                .isbn(isbn)
                .status(Book.BookStatus.AVAILABLE)
                .build();
    }

    // 重载方法，提供更多创建选项
    public Book createBook(String title, String author, BigDecimal price) {
        return createBook(title, author, price, LocalDate.now(), "");
    }
}
