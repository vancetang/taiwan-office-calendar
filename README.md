# Taiwan Office Calendar (台灣行政機關辦公日曆表)

[![CodeFactor](https://www.codefactor.io/repository/github/vancetang/taiwan-office-calendar/badge)](https://www.codefactor.io/repository/github/vancetang/taiwan-office-calendar)
![Spring Boot](https://img.shields.io/badge/dynamic/xml?url=https://raw.githubusercontent.com/vancetang/taiwan-office-calendar/main/pom.xml&query=//*[local-name()='parent']/*[local-name()='version']&label=Spring%20Boot&color=brightgreen)
![Java Version](https://img.shields.io/badge/dynamic/xml?url=https://raw.githubusercontent.com/vancetang/taiwan-office-calendar/main/pom.xml&query=//*[local-name()='properties']/*[local-name()='java.version']&label=Java&color=ED8B00&logo=openjdk&logoColor=white)
![License](https://img.shields.io/badge/License-MIT-yellow)

提供台灣國定假日與補班日查詢的 RESTful API 與線上月曆，資料同步自臺北市資料大平臺。本專案基於 Spring Boot 開發，自動擷取、處理並展示行政機關辦公日曆表。

## 主要功能

1.  **自動化資料擷取**: 支援從政府資料開放平臺下載 CSV 格式的行事曆資料。
2.  **標準化 JSON 輸出**: 將原始資料轉換為易於使用的 JSON 格式 (依年份分類)。
3.  **RESTful API**: 提供查詢特定年份假日或即時放假資訊 (介接 NCDR) 的 API。
4.  **視覺化月曆**: 內建 Web 介面 (類似 Google Calendar)，直觀顯示上班日與放假日。

## 資料格式說明

本專案在執行資料擷取與處理後，會於 `src/main/resources/static/opendata/holiday/` 目錄下產生 JSON 格式的資料檔案，主要包含以下兩種檔案：

### 1. 年份索引檔 (years.json)
記錄目前系統已產生的年份列表，按降序排列，方便前端動態讀取年份下拉選單：
```json
[
  "2026",
  "2025",
  "2024"
]
```

### 2. 年度假日資料檔 (例如 2024.json)
包含該年度的所有假日、補假與補班日資訊，欄位說明如下：

| 欄位名稱          | 資料型態 | 說明                                                                        | 範例值                         |
| :---------------- | :------- | :-------------------------------------------------------------------------- | :----------------------------- |
| `date`            | String   | 日期 (格式: `yyyyMMdd`)                                                     | `"20240101"`                   |
| `year`            | String   | 西元年份 (格式: `yyyy`)                                                     | `"2024"`                       |
| `name`            | String   | 節日或假日名稱 (補行上班或無特定節日名稱時可能為空字串)                     | `"中華民國開國紀念日"`         |
| `isHoliday`       | boolean  | 是否為放假日 (`true` 代表放假，`false` 代表需要上班或補行上班)              | `true`                         |
| `holidayCategory` | String   | 假日類別 (如：放假之紀念日及節日、補假、補行上班、星期六、星期日、特定節日) | `"放假之紀念日及節日"`         |
| `description`     | String   | 原始資料的詳細描述或說明 (常包含彈性放假或補班的依據與緣由)                 | `"中華民國開國紀念日(1月1日)"` |
| `note`            | String   | 系統自動分析關聯後的備註 (例如標示補班/調整放假的關聯原始節日)              | `"春節"`                       |

#### JSON 資料範例：
```json
[
  {
    "date": "20240101",
    "year": "2024",
    "name": "中華民國開國紀念日",
    "isHoliday": true,
    "holidayCategory": "放假之紀念日及節日",
    "description": "中華民國開國紀念日(1月1日)",
    "note": null
  },
  {
    "date": "20240217",
    "year": "2024",
    "name": "",
    "isHoliday": false,
    "holidayCategory": "補行上班",
    "description": "因應除夕及春節假期調整2月8日(星期四)為放假日，於2月17日(星期六)補行上班。",
    "note": "春節"
  }
]
```

## 資料處理規則

為了符合一般大眾的休假習慣，本系統針對特定節日進行了額外處理：

- **特定身分節日**: 部分節日僅適用於特定職業（如軍人、警察），一般民眾照常上班。系統會將這些日期的 `isHoliday` 標記為 `false`，類別設為「特定節日」。
  - **軍人節 (9/3)**
  - **警察節 (6/15)**

## 技術架構

- **語言**: Java 21
- **框架**: Spring Boot 3.5.8
- **建置工具**: Maven

## 快速開始

### 1. 啟動伺服器 (開發模式)
啟動 Web 伺服器 (預設 Port 8080)，可瀏覽月曆介面。
```powershell
mvn spring-boot:run
```
- 月曆首頁: http://localhost:8080/

### 2. 執行資料更新 (任務模式)
僅執行資料下載與處理任務，不啟動 Web Server (適合 CI/CD)。

#### 抓取並處理 (Fetch & Process)
從來源下載最新 CSV 並進行處理：
```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--job=fetch"
```

#### 僅處理 (Process Only)
僅重新解析現有的 JSON 檔案 (例如修復關聯資料邏輯)，不重新下載：
```powershell
mvn spring-boot:run "-Dspring-boot.run.arguments=--job=process"
```

## 資料來源
- [臺北市資料大平臺](https://data.taipei/) -> [臺北市政府行政機關辦公日曆表](https://data.taipei/dataset/detail?id=c30ca421-d935-4faa-b523-9c175c8de738)
