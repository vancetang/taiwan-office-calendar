# ============================================================================
# auto-update.ps1 - 自動抓取 OpenData 並提交至 Git
# ============================================================================

$ErrorActionPreference = "Stop"

# 1. 執行 Spring Boot 任務模式 (只抓資料，抓完自動結束)
Write-Host "🚀 Starting Data Fetch Job..." -ForegroundColor Cyan
try {
    mvn spring-boot:run "-Dspring-boot.run.arguments=--job=fetch" -q
    if ($LASTEXITCODE -ne 0) {
        throw "Maven process exited with code $LASTEXITCODE"
    }
    Write-Host "✅ Data Fetch Completed." -ForegroundColor Green
}
catch {
    Write-Host "❌ Data Fetch Failed: $_" -ForegroundColor Red
    exit 1
}

# 2. 檢查 Git 狀態
$gitStatus = git status --porcelain -- src/main/resources/static/opendata/holiday/
if ([string]::IsNullOrWhiteSpace($gitStatus)) {
    Write-Host "✨ No changes detected. Skipping git commit." -ForegroundColor Yellow
    exit 0
}

# 3. Git Commit & Push (遵循 Conventional Commits 規範)
Write-Host "📦 Changes detected. Committing to Git..." -ForegroundColor Cyan

$date = Get-Date -Format "yyyy-MM-dd"
git add src/main/resources/static/opendata/holiday/*.json

# Conventional Commits 格式: chore(data): 自動更新假日資料
git commit -m "chore(data): auto-update holiday data on $date"

# 注意：這一步需要您的環境已經設定好 Git Credential Helper，否則會詢問帳密
Write-Host "⬆️ Pushing to GitHub..." -ForegroundColor Cyan
# git push origin main  # <-- 請自行解除註解並確認 branch 名稱

Write-Host "🎉 All Done!" -ForegroundColor Green
