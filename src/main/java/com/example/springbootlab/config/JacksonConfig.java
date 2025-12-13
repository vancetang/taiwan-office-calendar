package com.example.springbootlab.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;

/**
 * Jackson 序列化設定類別。
 *
 * <p>
 * 提供全域共用的 {@link ObjectMapper} Bean，
 * 避免各處自行建立實例造成設定不一致。
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@Configuration
public class JacksonConfig {

    /**
     * 建立並配置全域 ObjectMapper Bean。
     *
     * <p>
     * 配置項目：
     * <ul>
     * <li>啟用 JSON 格式化輸出 (Pretty Print)</li>
     * </ul>
     * </p>
     *
     * @return 配置完成的 ObjectMapper 實例
     */
    @Bean
    @Primary
    public ObjectMapper objectMapper() {
        return new ObjectMapper()
                .enable(SerializationFeature.INDENT_OUTPUT);
    }
}

