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
