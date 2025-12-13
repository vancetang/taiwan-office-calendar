package com.example.springbootlab;

import java.util.List;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.core.env.SimpleCommandLinePropertySource;
import org.springframework.lang.NonNull;

import org.springframework.cache.annotation.EnableCaching;

import com.example.springbootlab.service.FetchDataService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Spring Boot Lab 應用程式進入點。
 *
 * <p>
 * 實作 {@link ApplicationRunner} 以支援命令列參數解析，
 * 提供兩種執行模式：Server Mode 與 Task Mode。
 * </p>
 *
 * @author Spring Boot Lab
 * @since 1.0.0
 */
@Slf4j
@RequiredArgsConstructor
@SpringBootApplication
@ConfigurationPropertiesScan
@EnableCaching
public class SpringBootLabApplication implements ApplicationRunner {

    /** 資料抓取服務（建構子注入） */
    private final FetchDataService fetchDataService;

    /** 判斷 job 參數的常數 */
    private static final String JOB_FETCH = "fetch";
    private static final String JOB_PROCESS = "process";

    /**
     * 應用程式主入口。
     *
     * <p>
     * 在啟動 Spring Context 之前，先檢查命令列參數以決定 Web 應用程式類型：
     * <ul>
     * <li>{@code --job=fetch} 或 {@code --job=process}: 不啟動 Web Server (Task
     * Mode)</li>
     * <li>無參數或其他參數: 啟動 Web Server (Server Mode)</li>
     * </ul>
     * </p>
     *
     * @param args 命令列參數
     */
    public static void main(@NonNull String[] args) {
        // 在啟動 Context 之前檢查參數，決定是否啟動 Web 應用程式
        boolean isTaskMode = isTaskModeFromArgs(args);

        ConfigurableApplicationContext context = new SpringApplicationBuilder(SpringBootLabApplication.class)
                .web(isTaskMode ? WebApplicationType.NONE : WebApplicationType.SERVLET)
                .run(args);

        // Task Mode: 優雅關閉 Spring Context
        if (isTaskMode) {
            int exitCode = SpringApplication.exit(context, () -> 0);
            System.exit(exitCode);
        }
    }

    /**
     * 從命令列參數判斷是否為 Task Mode (fetch 或 process)。
     *
     * @param args 命令列參數
     * @return 是否為 Task Mode
     */
    private static boolean isTaskModeFromArgs(@NonNull String[] args) {
        SimpleCommandLinePropertySource source = new SimpleCommandLinePropertySource(args);
        String job = source.getProperty("job");
        return JOB_FETCH.equals(job) || JOB_PROCESS.equals(job);
    }

    /**
     * 應用程式啟動後的回調方法。
     *
     * <p>
     * 根據命令列參數決定執行模式：
     * <ul>
     * <li>Task Mode: 僅執行資料抓取任務，完成後結束程式</li>
     * <li>Server Mode: 執行初始資料抓取，然後保持伺服器運行</li>
     * </ul>
     * </p>
     *
     * @param args 應用程式參數
     * @throws Exception 當執行過程中發生錯誤時
     */
    @Override
    public void run(ApplicationArguments args) throws Exception {
        log.info("Spring Boot Lab 環境初始化完成。");

        // 解析 job 參數
        String jobType = null;
        if (args.containsOption("job")) {
            List<String> values = args.getOptionValues("job");
            if (values != null && !values.isEmpty()) {
                jobType = values.get(0);
            }
        }

        if (JOB_FETCH.equals(jobType)) {
            log.info("執行模式: TASK MODE - 僅抓取資料...");
            fetchDataService.fetchAndProcess();
            log.info("任務完成，程式即將結束。");
        } else if (JOB_PROCESS.equals(jobType)) {
            log.info("執行模式: TASK MODE - 僅處理現有檔案...");
            fetchDataService.processExistingFiles();
            log.info("任務完成，程式即將結束。");
        } else {
            // 預設: Server Mode (不自動抓取資料，需透過 Task Mode 手動更新)
            log.info("執行模式: SERVER MODE");
            log.info("伺服器啟動完成，等待請求中。");
            log.info("如需更新資料，請使用 Task Mode: --job=fetch 或 --job=process");
        }
    }
}
