# AGENTS.md

## 建置與測試指令

這是一個使用 Java 21 和 Maven 的 Spring Boot 3.5.9 專案。

### 常用指令
- `mvn spring-boot:run` - 啟動開發伺服器，預設連接埠為 8080
- `mvn spring-boot:run "-Dspring-boot.run.arguments=--job=fetch"` - 擷取並處理假日資料
- `mvn spring-boot:run "-Dspring-boot.run.arguments=--job=process"` - 僅處理現有的 JSON 資料
- `mvn clean install` - 建置專案並安裝至本地儲存庫
- `mvn test` - 執行所有測試
- `mvn test -Dtest=ClassName` - 執行特定類別中的所有測試
- `mvn test -Dtest=ClassName#methodName` - 執行單一測試方法
- `mvn clean package` - 建置 JAR 檔（產出於 `target/*.jar`）

## 程式碼風格指南

### 通用格式
- Java 檔案使用 4 個空格縮排（詳見 .editorconfig）
- YAML、JSON、CSS、JavaScript、HTML 使用 2 個空格縮排
- 換行符號：LF（非 CRLF）
- 檔案末尾插入一行空行
- 刪除行尾空白（Markdown 檔案除外）
- 使用 UTF-8 編碼

### 匯入 (Imports)
- 整理匯入順序：java.*、第三方函式庫 (org.*)、專案匯入 (com.example.*)
- 禁止使用萬用字元匯入（例如：避免使用 `import java.util.*;`）
- 每個匯入佔用獨立一行

### 類別結構
1. 套件宣告 (Package declaration)
2. 匯入 (Imports，依上述順序整理)
3. 類別層級的 Javadoc，包含 `@author` 與 `@since` 標籤
4. 註解 (Annotations，順序：@Slf4j、其他 Spring 註解如 @Service/@RestController、@RequiredArgsConstructor)
5. 類別宣告
6. 靜態常數 (SCREAMING_SNAKE_CASE)
7. 帶有 Javadoc 的實例欄位 (Instance fields)
8. 建構子（若需要）
9. 帶有 Javadoc 的方法

### 命名慣例
- 類別：PascalCase（例如：`HolidayController`、`RealTimeHolidayService`）
- 方法：小寫開頭的 camelCase（例如：`getHolidaysByYear`、`isTaipeiCityAllArea`）
- 常數：SCREAMING_SNAKE_CASE（例如：`NCDR_API_URL`、`TARGET_CITY_1`）
- 實例欄位：camelCase（例如：`holidayCache`、`objectMapper`）
- 測試方法：`testMethodName_Scenario()`（例如：`testIsTaipeiCityAllArea_Positive`）

### Lombok 使用方式
- 使用 `@Slf4j` 進行日誌記錄
- 使用 `@RequiredArgsConstructor` 進行基於建構子的相依性注入
- 模型類別使用 `@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`
- 將相依性注入的欄位標記為 final（Lombok 會生成對應的建構子）

### Javadoc 與註解
- 所有公開類別與方法皆須提供 Javadoc 註解
- 本專案的註解與 Javadoc 請使用**繁體中文**
- 非 void 方法須包含 `@param` 與 `@return` 標籤
- 針對複雜邏輯編寫具描述性的行內註解
- 在 Javadoc 中使用 `<p>` 標籤進行段落分隔

### 錯誤處理
- 使用自定義異常類別（例如：`ResourceNotFoundException`）
- 拋出異常時須附帶具描述性的訊息（使用繁體中文）
- 在服務層使用 try-catch 處理異常
- 使用 `@Slf4j` 記錄錯誤：`log.error("描述：{}", e.getMessage(), e)`
- 使用 `@RestControllerAdvice` 進行全域異常處理
- 回傳一致的錯誤回應格式，包含時間戳記 (timestamp)、狀態 (status)、錯誤 (error) 與訊息 (message)

### 服務層 (Service Layer) 模式
- 服務類別使用 `@Service` 註解標記
- 針對適合快取的方法使用 `@Cacheable`（包含 `unless` 條件）
- 使用 Spring 的 `RestClient` 進行 HTTP 呼叫，並透過建構子配置
- 優雅地處理外部 API 失敗（回傳空清單、記錄錯誤）
- 使用 Stream API 處理集合（`.stream().filter(...).toList()`）

### 控制層 (Controller Layer) 模式
- 控制器使用 `@RestController` 與 `@RequestMapping` 標記
- 透過 final 欄位 + `@RequiredArgsConstructor` 注入相依性
- 使用適當的 HTTP 註解（@GetMapping、@PostMapping 等）
- 在處理前驗證輸入參數（使用正規表示式進行格式驗證）
- 針對缺失的資源拋出 `ResourceNotFoundException`
- 針對頻繁存取的資料使用快取（例如：`ConcurrentHashMap`）

### 測試準則
- 使用 JUnit 5 (Jupiter)
- 必要時使用反射 (Reflection) 測試私有方法
- 將相關的測試方法群組化至具描述性的測試類別中
- 使用具描述性的測試方法名稱：`testFeature_Scenario()`
- 使用 `@BeforeEach` 進行共通的測試設定
- 測試正向、反向及邊界案例

### 日誌記錄 (Logging)
- 使用 `@Slf4j` 註解
- 使用適當的日誌等級：`log.debug()` 用於詳細資訊、`log.warn()` 用於警告、`log.error()` 用於錯誤
- 在日誌訊息中包含上下文（嘗試了什麼、發生了什麼）
- 格式：`log.error("描述：{}", e.getMessage(), e)` 以包含堆疊追蹤 (stacktrace)

### Spring 設定
- 針對不應為 null 的建構子參數使用 `@NonNull` 註解
- 在建構子中配置外部服務（如 RestClient）
- 使用 `application.yml` 管理設定屬性
- 透過 `@ConfigurationProperties` 類別存取設定（例如：`OpendataProperties`）

### 模型類別 (Model Classes)
- 使用 Lombok 註解：`@Data`、`@Builder`、`@NoArgsConstructor`、`@AllArgsConstructor`
- 包含欄位層級的 Javadoc 註解
- 使用適當的資料類型（特定格式的日期使用 String，旗標使用 boolean）
- 命名欄位時考慮反序列化需求
