@echo off
chcp 65001 >nul
setlocal

REM ==========================================
REM  Cloudflare Tunnel 启动器
REM  前提: 后端已在 127.0.0.1:8088 运行
REM ==========================================

cd /d "%~dp0"

set CF_EXE=D:\ruangong\cloudflared\cloudflared.exe
set CF_CONFIG=%~dp0config.yml

echo.
echo  ==========================================
echo   Cloudflare Tunnel 启动器
echo  ==========================================
echo  配置文件: %CF_CONFIG%
echo  日志文件: %~dp0cloudflared.log
echo  ==========================================
echo.

REM 检查 cloudflared.exe
if not exist "%CF_EXE%" (
    echo [错误] 找不到 cloudflared.exe
    echo 请从 https://github.com/cloudflare/cloudflared/releases 下载
    pause
    exit /b 1
)

REM 检查配置文件
if not exist "%CF_CONFIG%" (
    echo [错误] 找不到 config.yml
    echo 请先运行 setup-tunnel.bat 初始化
    pause
    exit /b 1
)

REM 检查后端是否启动
echo [信息] 检查后端服务 (127.0.0.1:8088) ...
powershell -Command "try { $r = Invoke-WebRequest -Uri 'http://127.0.0.1:8088/api/public/health' -UseBasicParsing -TimeoutSec 3; Write-Host '  [OK] 后端已运行 -' $r.Content } catch { Write-Host '  [警告] 后端未运行！请先启动后端' }"
echo.

REM 验证配置
echo [信息] 验证 Tunnel 配置 ...
"%CF_EXE%" tunnel --config "%CF_CONFIG%" ingress validate
if errorlevel 1 (
    echo [错误] Tunnel 配置验证失败，请检查 config.yml
    pause
    exit /b 1
)

REM 启动隧道
echo [信息] 启动 Cloudflare Tunnel ...
echo  按 Ctrl+C 停止
echo.

"%CF_EXE%" tunnel --config "%CF_CONFIG%" run

pause
