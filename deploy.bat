@echo off
chcp 6501 >nul
setlocal

echo.
echo  ==========================================
echo   工资管理系统 - 一键部署脚本
echo  ==========================================
echo.

set PROJECT_ROOT=%~dp0
set FRONTEND_DIR=%PROJECT_ROOT%frontend
set BACKEND_DIR=%PROJECT_ROOT%backend
set STATIC_DIR=%BACKEND_DIR%\src\main\resources\static

REM Step 1: 构建前端
echo [1/4] 构建前端 ...
cd /d "%FRONTEND_DIR%"
call npm run build
if errorlevel 1 (
    echo [错误] 前端构建失败！
    pause
    exit /b 1
)
echo       前端构建完成
echo.

REM Step 2: 复制前端到后端 static 目录
echo [2/4] 部署前端资源 ...
if exist "%STATIC_DIR%\assets" rmdir /s /q "%STATIC_DIR%\assets"
xcopy /E /I /Y "%FRONTEND_DIR%\dist\assets" "%STATIC_DIR%\assets" >nul
copy /Y "%FRONTEND_DIR%\dist\index.html" "%STATIC_DIR%\index.html" >nul
echo       前端资源已部署到 static/
echo.

REM Step 3: 构建后端 JAR
echo [3/4] 构建后端 JAR ...
cd /d "%BACKEND_DIR%"
call mvn package -DskipTests -q
if errorlevel 1 (
    echo [错误] 后端构建失败！
    pause
    exit /b 1
)
echo       后端 JAR 构建完成
echo.

REM Step 4: 启动服务（生产模式）
echo [4/4] 启动服务（生产模式）...
echo.
echo  ==========================================
echo   服务启动中 ...
echo   本地访问: http://127.0.0.1:8088
echo   公网访问: https://salary.feiyu.rest
echo   (需要同时运行 Cloudflare Tunnel)
echo  ==========================================
echo.

java -jar "%BACKEND_DIR%\target\*.jar" ^
    --spring.profiles.active=prod ^
    --server.port=8088 ^
    --server.address=127.0.0.1

pause
