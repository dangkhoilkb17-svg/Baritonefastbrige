@rem
@rem Gradle start-up script for Windows
@rem

@echo off
setlocal EnableExtensions EnableDelayedExpansion

set "APP_HOME=%~dp0"
if "%APP_HOME:~-1%"=="\" set "APP_HOME=%APP_HOME:~0,-1%"
set "GRADLE_VERSION=8.10.2"
set "GRADLE_HOME=%USERPROFILE%\.gradle\wrapper\dists\bfb-bootstrap-%GRADLE_VERSION%"
set "GRADLE_ZIP=%TEMP%\gradle-%GRADLE_VERSION%-bin.zip"

if exist "%GRADLE_HOME%\gradle-%GRADLE_VERSION%\bin\gradle.bat" goto runGradle

where powershell.exe >nul 2>&1
if errorlevel 1 (
    echo ERROR: PowerShell is required to bootstrap Gradle %GRADLE_VERSION%.
    exit /b 1
)

echo Downloading Gradle %GRADLE_VERSION%...
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; $u='https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip'; Invoke-WebRequest -UseBasicParsing -Uri $u -OutFile '%GRADLE_ZIP%'"
if errorlevel 1 (
    echo ERROR: Failed to download Gradle %GRADLE_VERSION%.
    exit /b 1
)

if not exist "%GRADLE_HOME%" mkdir "%GRADLE_HOME%"
powershell.exe -NoProfile -ExecutionPolicy Bypass -Command "$ErrorActionPreference='Stop'; Expand-Archive -LiteralPath '%GRADLE_ZIP%' -DestinationPath '%GRADLE_HOME%' -Force"
if errorlevel 1 (
    echo ERROR: Failed to extract Gradle %GRADLE_VERSION%.
    exit /b 1
)

del /q "%GRADLE_ZIP%" >nul 2>&1

:runGradle
call "%GRADLE_HOME%\gradle-%GRADLE_VERSION%\bin\gradle.bat" %*
set "EXIT_CODE=%ERRORLEVEL%"
endlocal & exit /b %EXIT_CODE%
