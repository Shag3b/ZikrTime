# ZikrTime APK Builder with Auto JDK Download
# This script automatically downloads a portable JDK if needed

Write-Host "========================================" -ForegroundColor Cyan
Write-Host "ZikrTime APK Builder" -ForegroundColor Cyan
Write-Host "========================================" -ForegroundColor Cyan
Write-Host ""

$projectDir = $PSScriptRoot
$portableJdkDir = Join-Path $projectDir "portable_jdk"
$javaExe = Join-Path $portableJdkDir "bin\java.exe"

# Check if portable JDK exists
if (Test-Path $javaExe) {
    Write-Host "[OK] Using existing portable JDK" -ForegroundColor Green
} else {
    Write-Host "[INFO] Portable JDK not found. Downloading..." -ForegroundColor Yellow
    Write-Host ""

    # Create temp directory
    $tempDir = Join-Path $env:TEMP "zikr_jdk_download"
    New-Item -ItemType Directory -Force -Path $tempDir | Out-Null

    # JDK download URL (Microsoft OpenJDK 17 - portable zip)
    $jdkUrl = "https://aka.ms/download-jdk/microsoft-jdk-17.0.10-windows-x64.zip"
    $jdkZip = Join-Path $tempDir "jdk.zip"

    Write-Host "[INFO] Downloading JDK 17 (this may take a few minutes)..." -ForegroundColor Yellow
    try {
        # Download with progress
        $ProgressPreference = 'SilentlyContinue'
        Invoke-WebRequest -Uri $jdkUrl -OutFile $jdkZip -UseBasicParsing
        $ProgressPreference = 'Continue'
        Write-Host "[OK] Download complete" -ForegroundColor Green
    } catch {
        Write-Host "[ERROR] Failed to download JDK: $_" -ForegroundColor Red
        Write-Host ""
        Write-Host "Please download manually from: https://adoptium.net/temurin/releases/?version=17"
        Write-Host "Choose: Windows x64 JDK .zip"
        Write-Host "Extract to: $portableJdkDir"
        pause
        exit 1
    }

    Write-Host "[INFO] Extracting JDK..." -ForegroundColor Yellow
    try {
        # Extract zip
        Expand-Archive -Path $jdkZip -DestinationPath $tempDir -Force

        # Find the extracted JDK folder (it will have a version in the name)
        $extractedJdk = Get-ChildItem -Path $tempDir -Directory | Where-Object { $_.Name -like "jdk*" } | Select-Object -First 1

        if ($extractedJdk) {
            # Move to portable_jdk
            Move-Item -Path $extractedJdk.FullName -Destination $portableJdkDir -Force
            Write-Host "[OK] JDK extracted successfully" -ForegroundColor Green
        } else {
            throw "Could not find extracted JDK folder"
        }
    } catch {
        Write-Host "[ERROR] Failed to extract JDK: $_" -ForegroundColor Red
        pause
        exit 1
    } finally {
        # Cleanup
        Remove-Item -Path $tempDir -Recurse -Force -ErrorAction SilentlyContinue
    }
}

Write-Host ""
Write-Host "[INFO] Setting JAVA_HOME..." -ForegroundColor Yellow
$env:JAVA_HOME = $portableJdkDir
$env:PATH = "$portableJdkDir\bin;$env:PATH"

Write-Host "[INFO] Verifying Java..." -ForegroundColor Yellow
try {
    $javaVersion = & $javaExe -version 2>&1
    Write-Host $javaVersion[0] -ForegroundColor Green
} catch {
    Write-Host "[ERROR] Java verification failed" -ForegroundColor Red
    pause
    exit 1
}

Write-Host ""
Write-Host "[INFO] Building APK..." -ForegroundColor Yellow
Write-Host ""

# Build the APK
cd $projectDir
& ".\gradlew.bat" assembleDebug --no-daemon

if ($LASTEXITCODE -eq 0) {
    Write-Host ""
    Write-Host "========================================" -ForegroundColor Green
    Write-Host "[SUCCESS] APK Built Successfully!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
    Write-Host ""

    $apkPath = Join-Path $projectDir "app\build\outputs\apk\debug\app-debug.apk"
    if (Test-Path $apkPath) {
        Write-Host "APK Location:" -ForegroundColor Cyan
        Write-Host $apkPath -ForegroundColor Yellow
        Write-Host ""
        Write-Host "APK Size: $((Get-Item $apkPath).Length / 1MB) MB" -ForegroundColor Cyan
        Write-Host ""
        Write-Host "Opening output folder..." -ForegroundColor Cyan
        Start-Process (Split-Path $apkPath)
    }
} else {
    Write-Host ""
    Write-Host "[ERROR] Build failed. Check output above." -ForegroundColor Red
}

Write-Host ""
pause

