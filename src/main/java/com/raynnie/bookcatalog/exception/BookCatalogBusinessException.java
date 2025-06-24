package com.raynnie.bookcatalog.exception;

import java.io.Serializable;

/**
 * 图书目录服务业务异常类，用于封装服务层的业务逻辑错误
 * @Author Raynnie.J
 * @Date 2025/6/24 14:34
 */
public class BookCatalogBusinessException extends RuntimeException implements Serializable {

    private static final long serialVersionUID = 1L;

    /** 错误码，用于前端统一处理 */
    private final String errorCode;

    /**
     * 构造函数（带错误码和错误信息）
     * @param errorCode 错误码
     * @param message   错误信息
     */
    public BookCatalogBusinessException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    /**
     * 构造函数（带错误码、错误信息和原始异常）
     * @param errorCode 错误码
     * @param message   错误信息
     * @param cause     原始异常
     */
    public BookCatalogBusinessException(String errorCode, String message, Throwable cause) {
        super(message, cause);
        this.errorCode = errorCode;
    }

    /**
     * 获取错误码
     * @return 错误码
     */
    public String getErrorCode() {
        return errorCode;
    }
}
