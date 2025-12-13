package com.example.springbootlab.controller;

import java.io.File;
import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.springbootlab.config.OpendataProperties;
import com.example.springbootlab.exception.ResourceNotFoundException;
import com.example.springbootlab.model.Holiday;
import com.example.springbootlab.model.ncdr.NcdrEntry;
import com.example.springbootlab.service.RealTimeHolidayService;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 假日資料 RESTful API 控制器。
 *
 * <p>
 * 提供假日資料的查詢介面，資料來源為預先產生的 JSON 檔案。
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@RestController
@RequestMapping("/api/holidays")
public class HolidayController {

    /** JSON 反序列化器（由 Spring 注入） */
    private final ObjectMapper objectMapper;

    /** 開放資料設定屬性（由 Spring 注入） */
    private final OpendataProperties opendataProperties;
    
    /** 即時假日服務 */
    private final RealTimeHolidayService realTimeHolidayService;

    /** 假日資料快取 (Key: Year, Value: Holiday List) */
    private final Map<String, List<Holiday>> holidayCache = new ConcurrentHashMap<>();

    /**
     * 依年份取得假日資料。
     *
     * @param year 西元年份 (例如: 2024)
     * @return 該年份的假日資料列表
     * @throws ResourceNotFoundException 當指定年份的資料不存在時
     */
    @GetMapping("/{year}")
    public List<Holiday> getHolidaysByYear(@PathVariable String year) {
        // Validate input format to prevent path traversal
        if (!year.matches("^\\d{4}$")) {
            throw new ResourceNotFoundException("年份格式錯誤，僅允許 4 位數字");
        }

        return holidayCache.computeIfAbsent(year, key -> {
            File file = Paths.get(opendataProperties.holiday().outputDir(), key + ".json").toFile();

            if (!file.exists()) {
                log.warn("找不到 {} 年度的假日資料。", key);
                throw new ResourceNotFoundException("找不到 " + key + " 年度的假日資料");
            }

            try {
                return objectMapper.readValue(file, new TypeReference<List<Holiday>>() {
                });
            } catch (IOException e) {
                log.error("讀取 {} 年度假日資料時發生錯誤", key, e);
                throw new ResourceNotFoundException("無法讀取 " + key + " 年度的假日資料", e);
            }
        });
    }
    
    /**
     * 查詢即時停班停課資訊 (台北市全區)。
     * 
     * @return 符合條件的 NCDR 警報資料列表
     */
    @GetMapping("/realtime")
    public List<NcdrEntry> getRealTimeHolidays() {
        return realTimeHolidayService.getRealTimeHolidays();
    }
}
