# Simple Release APK Builder
Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ZikrTime APK Builder (Simple Mode)" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

# Try to find Android Studio's JDK
$possibleJdks = @(
    "C:\Program Files\Android\Android Studio\jbr",
    "C:\Program Files\Android\Android Studio\jre",
    "$env:LOCALAPPDATA\Android\Sdk\jbr",
    "$env:ProgramFiles\Java\jdk-17*",
    "$env:ProgramFiles\Java\jdk-11*"
)

$jdkFound = $null
foreach ($jdk in $possibleJdks) {
    $expanded = Get-Item $jdk -ErrorAction SilentlyContinue | Select-Object -First 1
    if ($expanded -and (Test-Path "$($expanded.FullName)\bin\java.exe")) {
        $jdkFound = $expanded.FullName
        break
    }
}

if ($jdkFound) {
    Write-Host "[OK] Found JDK at: $jdkFound" -ForegroundColor Green
    $env:JAVA_HOME = $jdkFound
    $env:PATH = "$jdkFound\bin;$env:PATH"
} else {
    Write-Host "[ERROR] No JDK found. Please install JDK 11 or 17." -ForegroundColor Red
    Write-Host ""
    Write-Host "You can download it from:" -ForegroundColor Yellow
    Write-Host "  - https://adoptium.net/temurin/releases/?version=17" -ForegroundColor Yellow
    Write-Host "  - Or install Android Studio which includes a JDK" -ForegroundColor Yellow
    Write-Host ""
    pause
    exit 1
}

Write-Host ""
Write-Host "Building Release APK..." -ForegroundColor Yellow
Write-Host ""

# Clean previous build
& ".\gradlew.bat" clean

# Build release APK
& ".\gradlew.bat" assembleRelease

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "BUILD SUCCESSFUL!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""

    $apkPath = "app\build\outputs\apk\release\app-release.apk"
    if (Test-Path $apkPath) {
        $apkSize = (Get-Item $apkPath).Length / 1MB
        Write-Host "APK Location: $apkPath" -ForegroundColor Cyan
        Write-Host "APK Size: $([math]::Round($apkSize, 2)) MB" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "You can now install this APK on your Android device!" -ForegroundColor Green
    }
} else {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Red
    Write-Host "BUILD FAILED!" -ForegroundColor Red
    Write-Host "========================================" -ForegroundColor Red
    Write-Host ""
    Write-Host "Check the error messages above for details." -ForegroundColor Yellow
}

Write-Host ""
pause

