@echo off
cd /d "%~dp0mod"
if not exist "jdk21\bin\javac.exe" (
  echo Downloading JDK 21 - about 190 MB, one time only...
  powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest 'https://api.adoptium.net/v3/binary/latest/21/ga/windows/x64/jdk/hotspot/normal/eclipse' -OutFile jdk.zip; Expand-Archive jdk.zip -DestinationPath jdktmp -Force; Remove-Item jdk.zip"
  for /d %%D in (jdktmp\*) do move "%%D" jdk21 >nul
  rmdir /s /q jdktmp 2>nul
)
if not exist "jdk21\bin\javac.exe" (
  echo Could not set up JDK 21. Check your internet connection and try again.
  if not "%1"=="nopause" pause
  exit /b 1
)
set "JAVA_HOME=%CD%\jdk21"
set "PATH=%JAVA_HOME%\bin;%PATH%"
if not exist "gradle-8.12\bin\gradle.bat" (
  echo Downloading Gradle 8.12...
  powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest https://services.gradle.org/distributions/gradle-8.12-bin.zip -OutFile gradle.zip; Expand-Archive gradle.zip -DestinationPath . -Force; Remove-Item gradle.zip"
)
call gradle-8.12\bin\gradle.bat --stop >nul 2>nul
call gradle-8.12\bin\gradle.bat build
if errorlevel 1 (
  echo Build failed.
  if not "%1"=="nopause" pause
  exit /b 1
)
echo Done. The launcher adds the mod to every Fabric 1.21.4 instance when you press Play.
if not "%1"=="nopause" pause
