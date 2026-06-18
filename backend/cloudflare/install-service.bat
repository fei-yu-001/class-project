@echo off
chcp 65001 >nul
setlocal

REM ==========================================
REM  注册 Cloudflare Tunnel 为 Windows 服务
REM  实现开机自动启动（无需手动运行 bat）
REM ==========================================

cd /d "%~dp0"

set CF_EXE=D:\ruangong\cloudflared\cloudflared.exe
set CF_CONFIG=%~dp0config.yml

echo.
echo  ╔══════════════════════════════════════════╗
echo  ║   注册 Cloudflare Tunnel 为系统服务      ║
echo  ╚══════════════════════════════════════════╝
echo.

if not exist "%CF_EXE%" (
    echo [错误] 找不到 cloudflared.exe
    pause
    exit /b 1
)
if not exist "%CF_CONFIG%" (
    echo [错误] 找不到 config.yml，请先运行 setup-tunnel.bat
    pause
    exit /b 1
)

REM 检查管理员权限
net session >nul 2>&1
if errorlevel 1 (
    echo [错误] 需要管理员权限！
    echo 请右键此文件 → "以管理员身份运行"
    pause
    exit /b 1
)

echo [信息] 安装 Cloudflare Tunnel 为 Windows 服务...
echo  服务名称: CloudflaredTunnel
echo  启动类型: 自动
echo.

"%CF_EXE%" service install --config "%CF_CONFIG%"

if errorlevel 1 (
    echo [错误] 安装失败
    pause
    exit /b 1
)

echo.
echo [OK] 服务安装成功！
echo.
echo  管理命令:
echo    启动: net start CloudflaredTunnel
echo    停止: net stop CloudflaredTunnel
echo    卸载: cloudflared service uninstall
echo.

pause
