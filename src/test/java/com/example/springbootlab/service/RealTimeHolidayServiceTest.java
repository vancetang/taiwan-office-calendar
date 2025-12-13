package com.example.springbootlab.service;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.example.springbootlab.model.ncdr.NcdrEntry;
import com.example.springbootlab.model.ncdr.NcdrSummary;
import com.fasterxml.jackson.databind.ObjectMapper;

class RealTimeHolidayServiceTest {

    private RealTimeHolidayService service;
    private Method isTaipeiCityAllAreaMethod;

    @BeforeEach
    void setUp() throws Exception {
        service = new RealTimeHolidayService(new ObjectMapper());

        // 使用反射存取 private 方法進行測試
        isTaipeiCityAllAreaMethod = RealTimeHolidayService.class.getDeclaredMethod("isTaipeiCityAllArea",
                NcdrEntry.class);
        isTaipeiCityAllAreaMethod.setAccessible(true);
    }

    @Test
    void testIsTaipeiCityAllArea_Positive() throws Exception {
        // 測試案例 1: 臺北市全區 (標準格式)
        assertTrue(invokeMethod("[停班停課通知]臺北市:今天停止上班、停止上課。"));

        // 測試案例 2: 台北市全區 (不同寫法)
        assertTrue(invokeMethod("[停班停課通知]台北市:今天停止上班、停止上課。"));

        // 測試案例 3: 全形冒號
        assertTrue(invokeMethod("[停班停課通知]臺北市：今天停止上班、停止上課。"));
    }

    @Test
    void testIsTaipeiCityAllArea_Negative() throws Exception {
        // 測試案例 1: 其他縣市
        assertFalse(invokeMethod("[停班停課通知]新北市:今天停止上班、停止上課。"));

        // 測試案例 2: 臺北市特定區 (北投區) - 應為 False
        // 根據 NCDR 格式，特定區通常寫為 "臺北市北投區:"
        assertFalse(invokeMethod("[停班停課通知]臺北市北投區:今天停止上班、停止上課。"));

        // 測試案例 3: 台北市特定區
        assertFalse(invokeMethod("[停班停課通知]台北市士林區:今天停止上班、停止上課。"));

        // 測試案例 4: 內容提及但非發布對象
        assertFalse(invokeMethod("[停班停課通知]基隆市:今天停止上班，台北市正常上班上課。"));
    }

    @Test
    void testIsTaipeiCityAllArea_EdgeCases() throws Exception {
        // Null Check
        assertFalse((boolean) isTaipeiCityAllAreaMethod.invoke(service, new NcdrEntry()));

        NcdrEntry entry = new NcdrEntry();
        entry.setSummary(new NcdrSummary());
        assertFalse((boolean) isTaipeiCityAllAreaMethod.invoke(service, entry));
    }

    private boolean invokeMethod(String text) throws Exception {
        NcdrEntry entry = new NcdrEntry();
        NcdrSummary summary = new NcdrSummary();
        summary.setText(text);
        entry.setSummary(summary);

        return (boolean) isTaipeiCityAllAreaMethod.invoke(service, entry);
    }
}
