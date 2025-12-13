# Gemini 開發指南 for Spring Boot Lab

本指南提供 Gemini Agent 如何與 `spring-boot-lab` 專案互動及貢獻的說明。它反映了專案特定的慣例、架構與工作流程。

## 1. 專案概觀

- **目的**: 這是一個 Spring Boot 專案，設計用於從台灣的開放資料平台擷取假日資料。它將資料從 CSV 格式處理成年度 JSON 檔案，並透過 RESTful API 和靜態網頁介面提供服務。
- **語言**: Java 21
- **框架**: Spring Boot 3.5.8
- **建置工具**: Maven

## 2. 關鍵檔案與目錄

- `pom.xml`: 定義所有專案依賴項 (Spring Boot、Jackson、commons-csv) 與建置組態。
- `src/main/java/com/example/springbootlab/service/FetchDataService.java`: 核心業務邏輯所在。此服務負責下載 CSV 資料、解析並寫入最終的 JSON 檔案。
- `src/main/java/com/example/springbootlab/controller/HolidayController.java`: RESTful API 控制器。它公開用於存取假日資料的端點。
- `src/main/resources/application.yml`: 主要設定檔。包含伺服器埠號和開放資料來源 URL 等設定。
- `src/main/resources/static/`: 靜態資源目錄。
  - `index.html`: 月曆版前端 UI（預設首頁，類似 Google Calendar）。
  - `simple.html`: 精簡版前端 UI (支援 CSV 下載)。
  - `detail.html`: 詳細版前端 UI (支援 CSV 下載)。
  - `opendata/holiday/`: 生成的 JSON 檔案 (例如 `2024.json`, `years.json`) 的目的地。
- `spec.md`: 官方系統規格文件。有關架構細節和需求，請參考此文件。

## 3. 開發工作流程與指令

請嚴格遵守以下指令與工作流程。

### 3.1. 執行應用程式

應用程式有兩種執行模式，如 `spec.md` 中所定義。

- **A) 伺服器模式 (開發預設)**:
  - **說明**: 啟動完整的 Web 應用程式，包含位於 8080 埠的 RESTful API 伺服器。**不會自動抓取資料**，需透過任務模式手動更新。
  - **指令**:
    ```bash
    mvn spring-boot:run
    ```
  - **訪問 UI**:
    - 月曆版: `http://localhost:8080/` 或 `http://localhost:8080/index.html`（預設）
    - 精簡版: `http://localhost:8080/simple.html`
    - 詳細版: `http://localhost:8080/detail.html`

- **B) 任務模式 (用於 CI/CD 或自動化)**:
  - **說明**: 僅執行 `fetch` 任務 (資料下載與處理)，而不啟動 Web 伺服器。程序在完成後即結束。這是自動化更新的建議模式。
  - **指令**:
    ```bash
    # 執行 fetch 任務 (下載 + 處理)
    mvn spring-boot:run "-Dspring-boot.run.arguments=--job=fetch"
    
    # 執行 process 任務 (僅處理現有檔案)
    mvn spring-boot:run "-Dspring-boot.run.arguments=--job=process"
    ```

### 3.2. 執行測試

- **說明**: 儘管單元測試尚未完全實作 (請參閱 `todolist.md`)，但請使用標準的 Maven 指令來執行它們。當新增功能時，您應當新增對應的單元測試。
- **指令**:
  ```bash
  mvn test
  ```

## 4. 程式碼與風格慣例

- **Git Commit 與 Pull Request 規範**: 遵循 [Conventional Commits](https://www.conventionalcommits.org/) 1.0.0。
  - **格式結構**: `<type>(<scope>): <description>`
  - **Type & Scope**: 必須使用 **英文** (例如 `feat`, `fix`, `frontend`, `core`)。
  - **Description (冒號後的簡述)**: 必須使用 **繁體中文**。
  - **Body (詳細內容)**: 必須使用 **繁體中文**。
- **CI/CD 輸出**: GitHub Actions 或其他自動化腳本的 Log 輸出訊息 (echo) 應使用**繁體中文**。
- **語言與註解**: 所有程式碼、註解與文件均以**繁體中文**撰寫。
- **程式碼風格**: 遵循標準的 Java 慣例。專案廣泛使用 Lombok 以減少樣板程式碼。請在適當之處使用 `@Data`、`@Slf4j` 等註解。
- **資料模型**: POJO (例如 `Holiday.java`) 是簡單的資料載體，定義在 `model` 套件中。
- **錯誤處理**: 對於新的 API 端點，請實作返回適當 HTTP 狀態碼的錯誤處理，如 `todolist.md` 中所述。例如，如果某年份的資源不可用，則返回 404 Not Found。

## 5. 如何貢獻

1.  **理解目標**: 分析使用者請求，並參考 `spec.md` 和 `todolist.md` 以了解上下文。
2.  **實作功能**:
    - 對於業務邏輯變更，請修改 `FetchDataService.java`。
    - 對於 API 變更，請修改 `HolidayController.java`。
3.  **新增單元測試**: 在 `src/test/java/` 下建立新的測試類別以驗證您的變更。
4.  **更新文件**: 如果您新增或變更功能，請相應地更新 `README.md` 和 `spec.md`。
5.  **驗證**: 在伺服器模式和任務模式下執行應用程式，以確保沒有破壞現有功能。執行 `mvn test`。