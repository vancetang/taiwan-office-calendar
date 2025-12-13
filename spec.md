# System Specification: Taiwan Office Calendar

## 1. 系統概述
	taiwan-office-calendar 旨在提供一個穩定、標準化的台灣行政機關辦公日曆服務。系統核心功能為「資料標準化」與「資訊服務化」。

## 2. 核心功能規格

### 2.1 資料處理 (Data Processing)
- **來源**: [臺北市資料大平臺](https://data.taipei/) - 臺北市政府行政機關辦公日曆表 (CSV)。
- **處理**: 解析 CSV，處理編碼 (BOM)，轉換為 Holiday 物件模型。
- **產出**:
  - opendata/holiday/{year}.json: 單一年度的完整日曆資料。
  - opendata/holiday/years.json: 系統支援的年份索引。

### 2.2 API 服務 (API Services)
- GET /api/holidays/{year}: 取得指定年份的完整辦公日曆 (包含假日與補班資訊)。
- GET /api/holidays/realtime: (選用) 介接 NCDR 災害示警或其他即時停班停課資訊。

### 2.3 使用者介面 (UI)
- **月曆檢視 (Calendar View)**: 響應式網頁，以月曆形式呈現。
- **顏色標示**:
  - **紅色**: 放假日 (國定假日、週末)。
  - **灰色**: 上班日 / 補班日。
  - **其他**: 特殊節日標註。

## 3. 系統架構
- **Backend**: Spring Boot Web
- **Frontend**: HTML5, JavaScript (Vanilla / Simple Libraries)
- **Data Storage**: Static JSON Files (File-based database for simplicity and high availability via CDN/Static Hosting).

## 4. 部署策略
- 支援 Docker 容器化部署。
- 靜態 JSON 檔案可部署至 GitHub Pages 或 AWS S3。
