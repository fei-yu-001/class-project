@echo off
chcp 65001 >nul
setlocal EnableDelayedExpansion

REM ==========================================
REM  Cloudflare Tunnel 一键初始化
REM  自动完成: 登录 → 创建隧道 → 配置DNS → 生成config.yml
REM ==========================================

cd /d "%~dp0"
set CF_EXE=D:\ruangong\cloudflared\cloudflared.exe
set CF_DIR=C:\Users\%USERNAME%\.cloudflared

echo.
echo  ╔══════════════════════════════════════════╗
echo  ║   Cloudflare Tunnel 一键初始化向导       ║
echo  ╚══════════════════════════════════════════╝
echo.

REM === 0. 检查 cloudflared.exe ===
if not exist "%CF_EXE%" (
    echo [错误] 找不到 cloudflared.exe
    echo 请从 https://github.com/cloudflare/cloudflared/releases 下载
    echo 放到: %~dp0
    pause
    exit /b 1
)

REM === 1. 检查是否已登录 ===
if exist "%CF_DIR%\cert.pem" (
    echo [OK] 已检测到 Cloudflare 登录证书
) else (
    echo [1/4] 登录 Cloudflare 账号...
    echo       将打开浏览器，请授权
    echo.
    "%CF_EXE%" tunnel login
    if errorlevel 1 (
        echo [错误] 登录失败
        pause
        exit /b 1
    )
    echo [OK] 登录成功
)

REM === 2. 输入域名 ===
echo.
echo [2/4] 配置域名
echo  请输入你在 Cloudflare 托管的域名
echo  例如: salary.example.com 或 app.example.com
echo.
set /p DOMAIN="  你的域名: "

if "%DOMAIN%"=="" (
    echo [错误] 域名不能为空
    pause
    exit /b 1
)

REM === 3. 创建隧道 ===
echo.
echo [3/4] 创建隧道...
set TUNNEL_NAME=salary-system

REM 检查是否已存在
"%CF_EXE%" tunnel list 2>nul | findstr /C:"%TUNNEL_NAME%" >nul 2>&1
if not errorlevel 1 (
    echo [!] 隧道 "%TUNNEL_NAME%" 已存在，跳过创建
) else (
    "%CF_EXE%" tunnel create %TUNNEL_NAME%
    if errorlevel 1 (
        echo [错误] 创建隧道失败
        pause
        exit /b 1
    )
    echo [OK] 隧道创建成功
)

REM === 4. 获取 Tunnel ID ===
echo.
echo [信息] 获取隧道 ID...
set TUNNEL_ID=
for /f "tokens=1" %%a in ('"%CF_EXE%" tunnel list 2>nul ^| findstr /C:"%TUNNEL_NAME%"') do (
    set TUNNEL_ID=%%a
)

if "%TUNNEL_ID%"=="" (
    echo [错误] 无法获取隧道 ID
    echo 请手动查看: cloudflared tunnel list
    pause
    exit /b 1
)
echo [OK] 隧道 ID: %TUNNEL_ID%

REM === 5. 配置 DNS ===
echo.
echo [4/4] 配置 DNS 路由...
echo  将 %DOMAIN% 指向隧道 %TUNNEL_NAME%...
"%CF_EXE%" tunnel route dns %TUNNEL_NAME% %DOMAIN%
if errorlevel 1 (
    echo [!] DNS 路由可能已存在，或域名不在你的 Cloudflare 账号中
    echo  如果域名已配置，可以忽略此警告
) else (
    echo [OK] DNS 路由配置成功
)

REM === 6. 生成生产级 config.yml ===
echo.
echo [信息] 生成 config.yml ...

(
echo # ==========================================
echo # Cloudflare Tunnel 生产级配置
echo # 由 setup-tunnel.bat 自动生成
echo # ==========================================
echo.
echo tunnel: %TUNNEL_ID%
echo credentials-file: %CF_DIR%\%TUNNEL_ID%.json
echo.
echo # -----------------------------------------------------------
echo # 协议与连接优化
echo # -----------------------------------------------------------
echo protocol: http2
echo grace-period: 30s
echo retries: 5
echo.
echo # -----------------------------------------------------------
echo # 转发规则
echo # -----------------------------------------------------------
echo ingress:
echo   - hostname: %DOMAIN%
echo     service: http://127.0.0.1:8088
echo     originRequest:
echo       connectTimeout: 15s
echo       noTLSVerify: false
echo       keepAliveConnections: 32
echo       keepAliveTimeout: 90s
echo       httpHostHeader: %DOMAIN%
echo       http2Origin: false
echo.
echo   # 兜底规则（必须保留）
echo   - service: http_status:404
echo.
echo # -----------------------------------------------------------
echo # 日志
echo # -----------------------------------------------------------
echo logfile: %~dp0cloudflared.log
echo loglevel: info
echo.
echo # -----------------------------------------------------------
echo # 本地指标监控
echo # 访问 http://127.0.0.1:2000/metrics
echo # -----------------------------------------------------------
echo metrics: 127.0.0.1:2000
) > "%~dp0config.yml"

echo [OK] config.yml 已生成

REM === 7. 验证配置 ===
echo.
echo [信息] 验证配置文件...
"%CF_EXE%" tunnel --config "%~dp0config.yml" ingress validate
if errorlevel 1 (
    echo [警告] 配置验证失败，请检查 config.yml
) else (
    echo [OK] 配置验证通过
)

REM === 8. 完成 ===
echo.
echo  ╔══════════════════════════════════════════╗
echo  ║           初始化完成！                    ║
echo  ╠══════════════════════════════════════════╣
echo  ║  域名:     https://%DOMAIN%
echo  ║  隧道ID:   %TUNNEL_ID%
echo  ║  配置文件: %~dp0config.yml
echo  ╠══════════════════════════════════════════╣
echo  ║  下一步:                                  ║
echo  ║  1. 运行 start-tunnel.bat 启动隧道        ║
echo  ║  2. 浏览器访问 https://%DOMAIN%
echo  ╚══════════════════════════════════════════╝
echo.
pause
