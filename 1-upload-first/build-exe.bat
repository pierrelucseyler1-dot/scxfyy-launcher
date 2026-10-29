@echo off
cd /d "%~dp0"
where npm >nul 2>nul
if errorlevel 1 (
  if not exist "tools\node\node.exe" (
    echo Downloading Node.js - one time only...
    powershell -NoProfile -Command "$ProgressPreference='SilentlyContinue'; Invoke-WebRequest https://nodejs.org/dist/v20.18.0/node-v20.18.0-win-x64.zip -OutFile node.zip; Expand-Archive node.zip -DestinationPath tools -Force; Rename-Item tools\node-v20.18.0-win-x64 node; Remove-Item node.zip"
  )
  set "PATH=%CD%\tools\node;%PATH%"
)
call npm install
if errorlevel 1 (
  echo npm install failed. Check your internet connection and try again.
  pause
  exit /b 1
)
call npm run dist
if errorlevel 1 (
  echo.
  echo The build failed. Copy the messages above and send them to Claude.
  echo If you see "Cannot create symbolic link", turn on Developer Mode in Windows Settings
  echo (Privacy and security, For developers) or right-click this file and Run as administrator, then try again.
  pause
  exit /b 1
)
echo.
echo Done. In the dist folder: scxfyy-launcher.exe runs with a double-click (no install),
echo and scxfyy-launcher-setup.exe installs it. Send either file to your players.
pause
