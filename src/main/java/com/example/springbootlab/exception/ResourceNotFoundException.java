package com.example.springbootlab.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

/**
 * 資源未找到例外。
 *
 * <p>
 * 當請求的資源不存在時拋出此例外，
 * 自動回應 HTTP 404 Not Found 狀態碼。
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@ResponseStatus(HttpStatus.NOT_FOUND)
public class ResourceNotFoundException extends RuntimeException {

    /**
     * 建立資源未找到例外。
     *
     * @param message 錯誤訊息
     */
    public ResourceNotFoundException(String message) {
        super(message);
    }

    /**
     * 建立資源未找到例外（含原因）。
     *
     * @param message 錯誤訊息
     * @param cause   原始例外
     */
    public ResourceNotFoundException(String message, Throwable cause) {
        super(message, cause);
    }
}

