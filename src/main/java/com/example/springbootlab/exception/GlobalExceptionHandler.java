package com.example.springbootlab.exception;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import lombok.extern.slf4j.Slf4j;

/**
 * 全域例外處理器。
 *
 * <p>
 * 統一處理 Controller 層拋出的例外，
 * 回傳一致的錯誤回應格式。
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理資源未找到例外。
     *
     * @param ex 資源未找到例外
     * @return HTTP 404 回應
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleResourceNotFound(ResourceNotFoundException ex) {
        log.warn("資源未找到: {}", ex.getMessage());
        return buildErrorResponse(HttpStatus.NOT_FOUND, ex.getMessage());
    }

    /**
     * 處理靜態資源未找到例外（如 favicon.ico）。
     *
     * <p>
     * 瀏覽器會自動請求 favicon.ico，若不存在只記錄 DEBUG 等級，
     * 避免日誌污染。
     * </p>
     *
     * @param ex 靜態資源未找到例外
     * @return HTTP 404 回應
     */
    @ExceptionHandler(org.springframework.web.servlet.resource.NoResourceFoundException.class)
    public ResponseEntity<Map<String, Object>> handleNoResourceFound(
            org.springframework.web.servlet.resource.NoResourceFoundException ex) {
        log.debug("靜態資源未找到: {}", ex.getResourcePath());
        return buildErrorResponse(HttpStatus.NOT_FOUND, "資源不存在: " + ex.getResourcePath());
    }

    /**
     * 處理其他未預期的例外。
     *
     * @param ex 例外
     * @return HTTP 500 回應
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<Map<String, Object>> handleGenericException(Exception ex) {
        log.error("未預期的錯誤", ex);
        return buildErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR, "伺服器內部錯誤");
    }

    /**
     * 建立標準化的錯誤回應。
     *
     * @param status  HTTP 狀態碼
     * @param message 錯誤訊息
     * @return 錯誤回應實體
     */
    private ResponseEntity<Map<String, Object>> buildErrorResponse(HttpStatus status, String message) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status", status.value());
        body.put("error", status.getReasonPhrase());
        body.put("message", message);
        return new ResponseEntity<>(body, status);
    }
}
