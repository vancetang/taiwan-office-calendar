package com.example.springbootlab.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * 假日資料模型。
 *
 * <p>
 * 對應政府開放資料平台的行政機關辦公日曆表資料結構。
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Holiday {

    /** 日期 (格式: yyyyMMdd，例如: 20240101) */
    private String date;

    /** 年份 (西元年，例如: 2024) */
    private String year;

    /** 節日名稱 (例如: 中華民國開國紀念日) */
    private String name;

    /** 是否為放假日 (true: 放假, false: 上班) */
    private boolean isHoliday;

    /** 假日類別 (例如: 放假之紀念日及節日、補假、星期六、星期日) */
    private String holidayCategory;

    /** 說明 (補充資訊，可能為空) */
    private String description;

    /** 備註 (程式自動分析的關聯資訊，例如補假來源節日) */
    private String note;
}
