package com.raynnie.bookcatalog.exception;

/**
 * @Author Raynnie.J
 * @Date 2025/6/24 14:35
 */
public enum BookCatalogErrorCode {
    // 通用业务错误
    BUSINESS_ERROR("BUSINESS_ERROR", "业务处理失败"),
    OPERATION_FAILED("OPERATION_FAILED", "操作执行失败"),

    // 图书相关错误
    BOOK_NOT_FOUND("BOOK_NOT_FOUND", "图书不存在"),
    BOOK_ALREADY_EXISTS("BOOK_ALREADY_EXISTS", "图书已存在"),
    BOOK_STATUS_INVALID("BOOK_STATUS_INVALID", "图书状态不合法"),

    // 分类相关错误
    CATEGORY_NOT_FOUND("CATEGORY_NOT_FOUND", "分类不存在"),
    CATEGORY_HAS_ASSOCIATED_BOOKS("CATEGORY_HAS_ASSOCIATED_BOOKS", "分类包含关联图书，无法删除"),

    // 其他业务错误
    INVALID_PARAMETER("INVALID_PARAMETER", "参数不合法");

    private final String code;
    private final String defaultMessage;

    BookCatalogErrorCode(String code, String defaultMessage) {
        this.code = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode() { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}
