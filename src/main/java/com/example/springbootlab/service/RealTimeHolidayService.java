package com.example.springbootlab.service;

import java.util.Collections;
import java.util.List;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.lang.NonNull;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

import com.example.springbootlab.model.ncdr.NcdrEntry;
import com.example.springbootlab.model.ncdr.NcdrHolidayResponse;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.extern.slf4j.Slf4j;

/**
 * 即時颱風假查詢服務。
 * 
 * <p>
 * 負責串接國家災害防救科技中心 (NCDR) 的即時警報 API，
 * 查詢特定縣市（目前鎖定台北市/臺北市）的停班停課資訊。
 * </p>
 */
@Slf4j
@Service
public class RealTimeHolidayService {

    private static final String NCDR_API_URL = "https://alerts.ncdr.nat.gov.tw/JSONAtomFeed.ashx?AlertType=33";
    private static final String TARGET_CITY_1 = "臺北市";
    private static final String TARGET_CITY_2 = "台北市";

    private final RestClient restClient;

    public RealTimeHolidayService(@NonNull ObjectMapper objectMapper) {
        this.restClient = RestClient.builder()
                .messageConverters(converters -> converters.add(new MappingJackson2HttpMessageConverter(objectMapper)))
                .build();
    }

    /**
     * 查詢即時停班停課資訊。
     *
     * @return 符合條件的 NcdrEntry 列表，若無或發生錯誤則回傳空列表。
     */
    @Cacheable(value = "realTimeHolidays", unless = "#result == null || #result.isEmpty()")
    public List<NcdrEntry> getRealTimeHolidays() {
        try {
            NcdrHolidayResponse response = restClient.get()
                    .uri(NCDR_API_URL)
                    .retrieve()
                    .body(NcdrHolidayResponse.class);

            if (response == null || response.getEntry() == null) {
                return Collections.emptyList();
            }

            return response.getEntry().stream()
                    .filter(this::isTaipeiCityAllArea)
                    .toList();

        } catch (Exception e) {
            log.error("查詢 NCDR 即時停班停課 API 失敗: {}", e.getMessage());
            return Collections.emptyList();
        }
    }

    /**
     * 判斷是否為台北市全區停班停課。
     * 
     * <p>
     * 判斷邏輯：
     * 1. 檢查 summary text 是否包含「臺北市」或「台北市」。
     * 2. 確保不包含其他更細的行政區名稱（這裡簡化為檢查是否直接接冒號，
     * 例如 "[停班停課通知]臺北市:" 表示全區）。
     * </p>
     * 
     * @param entry NCDR 資料項目
     * @return true 若判定為全區停班停課
     */
    private boolean isTaipeiCityAllArea(NcdrEntry entry) {
        if (entry.getSummary() == null || entry.getSummary().getText() == null) {
            return false;
        }

        String text = entry.getSummary().getText();

        // 檢查是否包含目標城市
        boolean hasCityName = text.contains(TARGET_CITY_1) || text.contains(TARGET_CITY_2);
        if (!hasCityName) {
            return false;
        }

        // 全區判斷邏輯：
        // 根據觀察，NCDR 格式通常為 "[停班停課通知]臺北市:..." (全區)
        // 或 "[停班停課通知]新北市瑞芳區:..." (特定區)
        // 因此我們檢查 "臺北市:" 或 "台北市:" 是否存在
        boolean isAllArea1 = text.contains(TARGET_CITY_1 + ":") || text.contains(TARGET_CITY_1 + "：");
        boolean isAllArea2 = text.contains(TARGET_CITY_2 + ":") || text.contains(TARGET_CITY_2 + "：");

        return isAllArea1 || isAllArea2;
    }
}
