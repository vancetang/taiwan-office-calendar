package com.example.springbootlab.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * 開放資料相關配置屬性
 *
 * @param holiday 假日資料相關配置
 */
@ConfigurationProperties(prefix = "opendata")
public record OpendataProperties(Holiday holiday) {

    /**
     * 假日資料配置
     *
     * @param url       資料來源 URL
     * @param outputDir 輸出目錄路徑
     */
    public record Holiday(String url, String outputDir) {
    }
}
