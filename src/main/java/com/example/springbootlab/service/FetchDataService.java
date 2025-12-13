package com.example.springbootlab.service;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.input.BOMInputStream;
import org.springframework.stereotype.Service;

import com.example.springbootlab.config.OpendataProperties;
import com.example.springbootlab.model.Holiday;
import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OpenData 資料抓取與處理服務。
 *
 * <p>
 * 負責從政府開放資料平台下載 CSV 檔案，解析後依年份分組，
 * 並輸出為 JSON 格式供前端使用。
 * </p>
 *
 * <p>
 * 處理流程：
 * <ol>
 * <li>下載 CSV 檔案至暫存區</li>
 * <li>解析 CSV 內容並轉換為 Holiday 物件</li>
 * <li>依年份分組並輸出 JSON 檔案</li>
 * <li>產生年份索引檔 (years.json)</li>
 * <li>清理暫存檔案</li>
 * </ol>
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@Service
public class FetchDataService {

    /** 下載連線逾時時間 (毫秒) */
    private static final int CONNECTION_TIMEOUT_MS = 10000;

    /** 下載讀取逾時時間 (毫秒) */
    private static final int READ_TIMEOUT_MS = 30000;

    /** 表示「是」的字串常數 */
    private static final String YES_STRING = "是";

    /** JSON 序列化器（由 Spring 注入） */
    private final ObjectMapper objectMapper;

    /** 開放資料設定屬性（由 Spring 注入） */
    private final OpendataProperties opendataProperties;

    /**
     * 僅處理現有 JSON 檔案，更新關聯資訊 (不重新下載)。
     */
    public void processExistingFiles() {
        try {
            Path outputDir = Paths.get(opendataProperties.holiday().outputDir());
            if (!Files.exists(outputDir)) {
                log.warn("輸出目錄不存在，無法處理現有檔案: {}", outputDir);
                return;
            }

            log.info("開始處理現有 JSON 檔案: {}", outputDir);

            List<Path> jsonFiles;
            try (var stream = Files.list(outputDir)) {
                jsonFiles = stream
                        .filter(Files::isRegularFile)
                        .filter(p -> p.toString().endsWith(".json"))
                        .filter(p -> !p.getFileName().toString().equals("years.json"))
                        .toList();
            }

            if (jsonFiles.isEmpty()) {
                log.info("沒有找到需要處理的 JSON 檔案。");
                return;
            }

            for (Path file : jsonFiles) {
                try {
                    // 1. 讀取 JSON
                    List<Holiday> holidays = loadHolidaysFromJson(file);
                    
                    // 2. 處理關聯節日
                    processRelatedHolidays(holidays);
                    
                    // 3. 寫回 JSON
                    saveHolidaysToJson(file, holidays);
                    
                    log.info("已更新檔案: {}", file.getFileName());
                } catch (IOException e) {
                    log.error("處理檔案失敗: {}", file, e);
                }
            }
            log.info("所有現有檔案處理完成。");

        } catch (IOException e) {
            log.error("掃描目錄失敗", e);
        }
    }

    /**
     * 執行資料抓取與處理的主要方法。
     *
     * <p>
     * 此方法會：
     * <ul>
     * <li>從設定的 URL 下載 CSV 檔案</li>
     * <li>解析 CSV 並建立 Holiday 物件列表</li>
     * <li>依年份分組輸出 JSON 檔案</li>
     * <li>產生年份索引檔供前端讀取</li>
     * </ul>
     * </p>
     */
    public void fetchAndProcess() {
        Path tempFile = null;
        String dataUrl = opendataProperties.holiday().url();
        try {
            log.info("開始從 OpenData 抓取資料: {}", dataUrl);

            // 步驟 1: 下載至暫存檔
            tempFile = downloadToTempFile(dataUrl);

            // 步驟 2: 解析 CSV
            List<Holiday> allHolidays = parseCsvFile(tempFile);
            log.info("成功解析 {} 筆記錄。", allHolidays.size());

            // 步驟 2.5: 處理關聯節日 (補假追蹤)
            processRelatedHolidays(allHolidays);

            // 步驟 3: 依年份分組並輸出 JSON
            Map<String, List<Holiday>> groupedByYear = groupByYear(allHolidays);
            writeYearlyJsonFiles(groupedByYear);

            // 步驟 4: 產生年份索引檔
            writeYearsIndex(groupedByYear);

        } catch (IOException e) {
            log.error("檔案處理過程發生 I/O 錯誤", e);
        } catch (URISyntaxException e) {
            log.error("資料來源 URL 格式錯誤: {}", dataUrl, e);
        } finally {
            // 步驟 5: 清理暫存檔
            cleanupTempFile(tempFile);
        }
    }

    /**
     * 從 JSON 檔案讀取 Holiday 列表。
     */
    private List<Holiday> loadHolidaysFromJson(Path file) throws IOException {
        try (Reader reader = new InputStreamReader(new FileInputStream(file.toFile()), StandardCharsets.UTF_8)) {
            return objectMapper.readValue(reader, new com.fasterxml.jackson.core.type.TypeReference<List<Holiday>>() {});
        }
    }

    /**
     * 將 Holiday 列表寫入 JSON 檔案。
     */
    private void saveHolidaysToJson(Path file, List<Holiday> holidays) throws IOException {
        writeJsonWithLf(file, holidays);
    }

    /**
     * 下載資料至暫存檔。
     *
     * @param dataUrl 資料來源 URL
     * @return 暫存檔路徑
     * @throws IOException        當下載失敗時
     * @throws URISyntaxException 當 URL 格式錯誤時
     */
    private Path downloadToTempFile(String dataUrl) throws IOException, URISyntaxException {
        Path tempFile = Files.createTempFile("holiday_data_", ".csv");
        log.info("下載檔案中...");
        FileUtils.copyURLToFile(
                new URI(dataUrl).toURL(),
                tempFile.toFile(),
                CONNECTION_TIMEOUT_MS,
                READ_TIMEOUT_MS);
        log.info("檔案下載成功: {}", tempFile);
        return tempFile;
    }

    /**
     * 解析 CSV 檔案並轉換為 Holiday 物件列表。
     *
     * @param csvFile CSV 檔案路徑
     * @return Holiday 物件列表
     * @throws IOException 當檔案讀取失敗時
     */
    private List<Holiday> parseCsvFile(Path csvFile) throws IOException {
        List<Holiday> holidays = new ArrayList<>();

        try (BOMInputStream bomIn = BOMInputStream.builder()
                .setInputStream(new FileInputStream(csvFile.toFile()))
                .get();
                Reader reader = new InputStreamReader(bomIn, StandardCharsets.UTF_8);
                CSVParser parser = CSVParser.builder()
                        .setReader(reader)
                        .setFormat(buildCsvFormat())
                        .get()) {

            for (CSVRecord record : parser) {
                holidays.add(mapToHoliday(record));
            }
        }
        return holidays;
    }

    /**
     * 建立 CSV 解析格式設定。
     *
     * @return CSVFormat 實例
     */
    private CSVFormat buildCsvFormat() {
        return CSVFormat.DEFAULT.builder()
                .setHeader()
                .setSkipHeaderRecord(true)
                .setIgnoreHeaderCase(true)
                .setTrim(true)
                .get();
    }

    /**
     * 將 CSV 記錄對應到 Holiday 物件。
     *
     * @param record CSV 記錄
     * @return Holiday 物件
     */
    private Holiday mapToHoliday(CSVRecord record) {
        String dateStr = record.get("Date");
        // 從日期字串 (YYYYMMDD) 擷取年份
        String year = dateStr.substring(0, 4);

        return Holiday.builder()
                .date(dateStr)
                .year(year)
                .name(record.get("name"))
                .isHoliday(YES_STRING.equals(record.get("isHoliday")))
                .holidayCategory(record.get("holidayCategory"))
                .description(record.get("description"))
                .build();
    }

    /**
     * 依年份分組 Holiday 資料。
     *
     * @param holidays Holiday 列表
     * @return 依年份分組的 Map
     */
    private Map<String, List<Holiday>> groupByYear(List<Holiday> holidays) {
        return holidays.stream()
                .collect(Collectors.groupingBy(Holiday::getYear));
    }

    /**
     * 輸出各年份的 JSON 檔案。
     *
     * @param groupedByYear 依年份分組的資料
     * @throws IOException 當檔案寫入失敗時
     */
    private void writeYearlyJsonFiles(Map<String, List<Holiday>> groupedByYear) throws IOException {
        Path outputPath = Paths.get(opendataProperties.holiday().outputDir());
        Files.createDirectories(outputPath);

        for (Map.Entry<String, List<Holiday>> entry : groupedByYear.entrySet()) {
            String year = entry.getKey();
            List<Holiday> holidaysOfYear = entry.getValue();

            Path jsonFile = outputPath.resolve(year + ".json");
            writeJsonWithLf(jsonFile, holidaysOfYear);
            log.info("已產生 {} 年度 JSON: {}", year, jsonFile.toAbsolutePath());
        }
    }

    /**
     * 產生年份索引檔 (years.json)。
     *
     * <p>
     * 此方法會掃描輸出目錄中所有現有的年份 JSON 檔案，
     * 確保索引包含所有年份，而非僅限於當次下載的資料。
     * </p>
     *
     * @param groupedByYear 依年份分組的資料（用於確保新年份也被包含）
     * @throws IOException 當檔案寫入失敗時
     */
    private void writeYearsIndex(Map<String, List<Holiday>> groupedByYear) throws IOException {
        Path outputPath = Paths.get(opendataProperties.holiday().outputDir());

        // 掃描目錄中所有 {year}.json 檔案，取得完整的年份列表
        List<String> allYears = new ArrayList<>(groupedByYear.keySet());

        try (var files = Files.list(outputPath)) {
            files.filter(Files::isRegularFile)
                    .map(Path::getFileName)
                    .map(Path::toString)
                    .filter(name -> name.matches("\\d{4}\\.json"))
                    .map(name -> name.replace(".json", ""))
                    .filter(year -> !allYears.contains(year))
                    .forEach(allYears::add);
        }

        List<String> sortedYears = allYears.stream()
                .sorted(Comparator.reverseOrder())
                .toList();

        Path yearsFile = outputPath.resolve("years.json");
        writeJsonWithLf(yearsFile, sortedYears);
        log.info("已產生年份索引檔 (共 {} 個年份): {}", sortedYears.size(), yearsFile.toAbsolutePath());
    }

    /**
     * 將物件寫入 JSON 檔案，使用 LF 換行符號。
     *
     * @param filePath 檔案路徑
     * @param data     要序列化的資料物件
     * @throws IOException 當檔案寫入失敗時
     */
    private void writeJsonWithLf(Path filePath, Object data) throws IOException {
        try (Writer writer = new OutputStreamWriter(Files.newOutputStream(filePath), StandardCharsets.UTF_8)) {
            String json = objectMapper.writeValueAsString(data);
            // 確保換行符號為 LF（移除可能的 CR）
            json = json.replace("\r\n", "\n").replace("\r", "\n");
            writer.write(json);
            // 確保檔案以 LF 結尾
            if (!json.endsWith("\n")) {
                writer.write("\n");
            }
        }
    }

    /**
     * 清理暫存檔案。
     *
     * @param tempFile 暫存檔路徑
     */
    private void cleanupTempFile(Path tempFile) {
        if (tempFile == null) {
            return;
        }
        try {
            boolean deleted = Files.deleteIfExists(tempFile);
            if (deleted) {
                log.info("暫存檔已刪除: {}", tempFile);
            }
        } catch (IOException e) {
            log.warn("無法刪除暫存檔: {}", tempFile, e);
        }
    }    /**
     * 處理關聯節日資訊。
     * <p>
     * 針對補假、補上班等項目，嘗試從其他節日的說明中找出關聯。
     * 例如：10/24 補假，會在 10/25 的說明中找到「於10月24日補假」，
     * 此時將 10/25 的節日名稱填入 10/24 的 note 欄位。
     * </p>
     *
     * @param holidays 所有節日列表
     */
    private void processRelatedHolidays(List<Holiday> holidays) {
        // 為避免重複解析，先建立年份分組的 Map，縮小搜尋範圍 (雖然題目需求是同一年，但全 list 跑也無妨，這邊優化一下)
        Map<String, List<Holiday>> byYear = groupByYear(holidays);

        for (List<Holiday> yearList : byYear.values()) {
            for (Holiday target : yearList) {
                // 判斷是否需要追蹤：補假、補上班、調整放假 或 名稱空白
                boolean isMakeup = false;
                if (target.getHolidayCategory() != null) {
                    String cat = target.getHolidayCategory();
                    if (cat.contains("補假") || cat.contains("補行上班") || cat.contains("調整放假")) {
                        isMakeup = true;
                    }
                }
                if (!isMakeup && (target.getName() == null || target.getName().trim().isEmpty())) {
                    isMakeup = true;
                }

                // 如果不是目標類型，跳過
                if (!isMakeup) {
                    continue;
                }

                // 解析日期
                String dStr = target.getDate();
                if (dStr == null || dStr.length() != 8) {
                    continue;
                }
                int month = Integer.parseInt(dStr.substring(4, 6));
                int day = Integer.parseInt(dStr.substring(6, 8));

                // 建立搜尋模式 (使用 Regex 以支援更多格式，如空白、補零等)
                String mStr = String.valueOf(month);
                String mPad = String.format("%02d", month);
                String mChi = toChineseNum(month);

                String dayStr = String.valueOf(day);
                String dPad = String.format("%02d", day);
                String dChi = toChineseNum(day);

                // 建構 Regex: (M|MM|中文) + 可能空白 + "月" + 可能空白 + (D|DD|中文) + 可能空白 + "日"
                // 注意：toChineseNum 回傳的若是單一數字可能與 String.valueOf 相同 (雖然目前實作 10 以下是中文)，但重複在 OR 條件中無妨
                String regex = String.format("(%s|%s|%s)\\s*月\\s*(%s|%s|%s)\\s*日", 
                        mStr, mPad, mChi, dayStr, dPad, dChi);
                
                java.util.regex.Pattern pattern = java.util.regex.Pattern.compile(regex);

                // 在同一年份的其他項目中搜尋
                for (Holiday source : yearList) {
                    if (source == target) continue;

                    String desc = source.getDescription();
                    if (desc != null) {
                        java.util.regex.Matcher matcher = pattern.matcher(desc);
                        if (matcher.find()) {
                            // 找到關聯，設定 note
                            String sourceName = source.getName();
                            if (sourceName != null && !sourceName.isEmpty()) {
                                target.setNote(sourceName);
                                // 找到一個就停止搜尋該項目的來源
                                break;
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * 將數字轉換為中文數字 (僅支援日期用途，1-31)。
     *
     * @param num 數字
     * @return 中文數字字串
     */
    private String toChineseNum(int num) {
        final String[] chinese = {"", "一", "二", "三", "四", "五", "六", "七", "八", "九", "十"};
        if (num <= 10) {
            return chinese[num];
        } else if (num < 20) {
            return "十" + (num % 10 == 0 ? "" : chinese[num % 10]);
        } else if (num < 30) {
            return "二十" + (num % 10 == 0 ? "" : chinese[num % 10]);
        } else if (num < 40) {
            return "三十" + (num % 10 == 0 ? "" : chinese[num % 10]);
        }
        return String.valueOf(num);
    }
}
