@echo off
setlocal

echo ========================================
echo ZikrTime APK Builder
echo ========================================
echo.

REM Check if portable JDK exists
if exist "portable_jdk\bin\java.exe" (
    echo [OK] Using existing portable JDK
    goto :build
)

echo [INFO] Portable JDK not found. Please download manually.
echo.
echo Please download JDK 17 from: https://adoptium.net/temurin/releases/?version=17
echo Choose: Windows x64 JDK .zip (not .msi)
echo.
echo Extract the downloaded zip to: %~dp0portable_jdk
echo Then run this script again.
echo.
pause
exit /b 1

:build
echo [INFO] Setting JAVA_HOME...
set "JAVA_HOME=%~dp0portable_jdk"
set "PATH=%JAVA_HOME%\bin;%PATH%"

echo [INFO] Verifying Java...
java -version
if errorlevel 1 (
    echo [ERROR] Java verification failed
    pause
    exit /b 1
)

echo.
echo [INFO] Building APK...
echo.
call gradlew.bat assembleDebug --no-daemon

if errorlevel 1 (
    echo.
    echo [ERROR] Build failed. Check output above.
    pause
    exit /b 1
)

echo.
echo ========================================
echo [SUCCESS] APK Built Successfully!
echo ========================================
echo.
echo APK Location:
echo %~dp0app\build\outputs\apk\debug\app-debug.apk
echo.
echo Opening output folder...
start "" "%~dp0app\build\outputs\apk\debug"
echo.
pause

