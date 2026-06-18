# Cloudflare Tunnel 公网部署指南

## 架构总览

```
全球用户 (任意网络)
    │
    │ HTTPS（自动 TLS）
    ▼
┌──────────────────────────────────┐
│        Cloudflare Edge           │
│  ┌────────────────────────────┐  │
│  │  CDN + DDoS 防护 + WAF    │  │  ← Cloudflare 自动提供
│  └────────────┬───────────────┘  │
│               │                  │
│  ┌────────────▼───────────────┐  │
│  │   Cloudflare Tunnel       │  │  ← 隧道入口
│  └────────────┬───────────────┘  │
└───────────────┼──────────────────┘
                │ 加密隧道（QUIC/HTTP2）
                ▼
┌──────────────────────────────────┐
│  你的电脑 (Windows)              │
│  ┌────────────────────────────┐  │
│  │  cloudflared.exe           │  │  ← 隧道客户端
│  └────────────┬───────────────┘  │
│               │ HTTP              │
│  ┌────────────▼───────────────┐  │
│  │  Spring Boot :8088         │  │  ← 后端 + 前端静态资源
│  │  ├─ /api/*       → 控制器  │  │
│  │  ├─ /assets/*    → 静态资源│  │
│  │  └─ /*           → SPA     │  │
│  └────────────────────────────┘  │
│  ┌────────────────────────────┐  │
│  │  MySQL :3306  │ Redis :6379│  │  ← 仅本地访问
│  └────────────────────────────┘  │
└──────────────────────────────────┘
```

**核心原理**: cloudflared 主动向 Cloudflare Edge 发起出站连接，无需开放任何入站端口。Cloudflare 收到用户请求后通过这条隧道转发到你的本地服务。

---

## 前提条件

| 项目 | 要求 |
|------|------|
| 域名 | 已在 Cloudflare 托管（DNS 托管即可，无需购买域名） |
| cloudflared.exe | 放在项目根目录（与 config.yml 同级） |
| Java 17+ | 后端运行环境 |
| MySQL + Redis | 本地运行，仅绑定 127.0.0.1 |
| 前端构建产物 | `npm run build` 后已复制到 `backend/src/main/resources/static/` |

---

## 一键部署（4 步）

### 步骤 1: 初始化隧道

双击 `setup-tunnel.bat`，按提示操作：

1. 自动检测登录状态（首次会打开浏览器授权）
2. 输入你的域名（如 `salary.example.com`）
3. 自动创建 Named Tunnel
4. 自动配置 DNS CNAME 路由
5. 自动生成 `config.yml`
6. 自动验证配置

完成后会在 `C:\Users\feiyu\.cloudflared\` 生成凭据文件。

### 步骤 2: 构建前端

```bash
cd salary-management/frontend
npm install
npm run build
```

将 `dist/` 目录下所有文件复制到 `salary-management/backend/src/main/resources/static/`。

### 步骤 3: 打包后端

在 IDEA 中执行 Maven 打包，或在命令行：

```bash
cd salary-management/backend
mvn clean package -DskipTests
```

生成 `target/salary-management-1.0.0.jar`。

### 步骤 4: 启动服务

双击 `start-all.bat`，脚本会自动：

1. 检测后端是否已运行
2. 启动后端（如未运行）
3. 智能等待后端就绪（轮询 `/api/public/health`，最多 90 秒）
4. 启动 Cloudflare Tunnel

浏览器访问 `https://你的域名` 即可看到登录页。

---

## 脚本说明

| 脚本 | 用途 | 说明 |
|------|------|------|
| `setup-tunnel.bat` | 一次性初始化 | 登录→创建隧道→DNS→生成config.yml |
| `start-all.bat` | 一键启动 | 后端 + Tunnel，智能等待就绪 |
| `start-tunnel.bat` | 单独启动 Tunnel | 后端已运行时使用 |
| `install-service.bat` | 注册 Windows 服务 | 开机自启，需管理员权限 |

---

## config.yml 配置详解

```yaml
tunnel: <TUNNEL_ID>              # 隧道 ID（setup-tunnel.bat 自动填充）
credentials-file: ...\.cloudflared\<TUNNEL_ID>.json

protocol: http2                  # 推荐值，性能最优
grace-period: 30s                # 优雅关闭等待时间
retries: 5                       # 连接重试次数

ingress:
  - hostname: your.domain.com    # 你的域名
    service: http://localhost:8088
    originRequest:
      connectTimeout: 30s        # 连接超时
      keepAliveConnections: 16   # 长连接池大小
      keepAliveTimeout: 90s      # 长连接超时
      httpHostHeader: your.domain.com  # 透传 Host 头
      http2Origin: false         # 后端是 HTTP/1.1
      noChunkedEncoding: false   # 透传真实 IP
  - service: http_status:404     # 兜底规则（必须保留）

metrics: 127.0.0.1:2000          # 本地监控指标
```

### 关键参数说明

| 参数 | 值 | 原因 |
|------|-----|------|
| `protocol: http2` | HTTP/2 | 比 QUIC 更稳定，支持多路复用，生产推荐 |
| `grace-period: 30s` | 30秒 | 关闭时等待现有请求完成，避免断连 |
| `retries: 5` | 5次 | 网络抖动时自动重连 |
| `http2Origin: false` | 关闭 | Spring Boot 默认 HTTP/1.1，开启会导致连接失败 |
| `httpHostHeader` | 域名 | Spring Boot 依赖 Host 头生成正确的重定向 URL |
| `keepAliveConnections: 16` | 16 | 减少TCP握手开销，适合中等并发 |

---

## 生产环境安全清单

### 必须做

- [ ] **修改默认超管密码**: 首次启动后用 `superadmin/superadmin123` 登录，立即修改
- [ ] **修改 JWT 密钥**: `set JWT_SECRET=你的强随机密钥（至少64字符）`
- [ ] **限制 CORS**: `set CORS_ALLOWED_ORIGINS=https://你的域名`
- [ ] **启用生产 Profile**: `set SPRING_PROFILES_ACTIVE=prod`
- [ ] **MySQL/Redis 仅本地访问**: 绑定 `127.0.0.1`，关闭公网端口

### 建议做

- [ ] **Cloudflare WAF**: 在 Cloudflare Dashboard → Security → WAF 启用托管规则
- [ ] **Cloudflare Rate Limiting**: Dashboard → Security → WAF → Rate limiting rules
- [ ] **SSL 模式设为 Full (Strict)**: Dashboard → SSL/TLS → Full (Strict)
- [ ] **Always Use HTTPS**: Dashboard → SSL/TLS → Edge Certificates → 开启
- [ ] **Minimum TLS 1.2**: Dashboard → SSL/TLS → Edge Certificates → 最低 TLS 版本
- [ ] **注册为 Windows 服务**: 运行 `install-service.bat`（需管理员权限）

### 可选做

- [ ] **Cloudflare Access**: 为管理后台添加额外认证层（邮箱验证/SSO）
- [ ] **Cloudflare Analytics**: 查看访问统计和威胁分析
- [ ] **自动 IP 白名单**: 在 Cloudflare WAF 中只允许特定国家/地区访问

---

## 开机自启方案

### 方案 1: Windows 服务（推荐）

1. 右键 `install-service.bat` → 以管理员身份运行
2. Tunnel 会注册为 `CloudflaredTunnel` 服务，开机自动启动
3. 后端也需注册为服务，使用 [NSSM](https://nssm.cc/):

```bash
# 下载 nssm.exe 后执行
nssm install SalaryBackend "C:\Program Files\Java\jdk-17\bin\java.exe" "-Xms512m -Xmx1024m -Dspring.profiles.active=prod -jar salary-management-1.0.0.jar"
nssm set SalaryBackend AppDirectory "C:\Users\feiyu\Desktop\数据库\课程设计\salary-management\backend"
nssm set SalaryBackend DisplayName "Salary Management Backend"
nssm set SalaryBackend Start SERVICE_AUTO_START
```

### 方案 2: 启动文件夹

将 `start-all.bat` 的快捷方式放到 `shell:startup` 目录（Win+R 输入 `shell:startup`）。

---

## 监控与运维

### 健康检查

```bash
# 后端健康状态（DB + Redis 连通性）
curl http://localhost:8088/api/public/health

# 返回示例
{"code":200,"message":"操作成功","data":{"status":"UP","timestamp":"2026-06-07T10:00:00","database":"UP","redis":"UP"}}
```

### Tunnel 指标

```bash
# 本地 metrics 端点（Prometheus 格式）
curl http://127.0.0.1:2000/metrics
```

关键指标：
- `cloudflared_tunnel_connections` — 活跃连接数
- `cloudflared_tunnel_request_errors` — 请求错误数
- `cloudflared_tunnel_server_locations` — 连接的 Cloudflare 数据中心

### 日志

| 日志 | 位置 |
|------|------|
| Tunnel 日志 | `课程设计/cloudflared.log` |
| 后端日志 | `课程设计/salary-management/backend/logs/salary-management.log` |

---

## 常见问题

### Q1: 访问域名显示 1033/1034 错误

Cloudflare 还没识别到隧道。等待 1-2 分钟，检查 `cloudflared.log` 是否有 `ERR` 或 `Connection registered` 日志。

### Q2: 出现 CORS 错误

启动时设置 `set CORS_ALLOWED_ORIGINS=https://你的域名`。不能用 `*` 配合 `allowCredentials=true`。

### Q3: 后端启动但 Tunnel 连不上

1. 检查 `cloudflared.log` 确认 `service: http://localhost:8088` 端口正确
2. 浏览器访问 `http://localhost:8088/api/public/health` 确认后端正常
3. 检查 `config.yml` 中 `credentials-file` 路径是否指向正确的 JSON 文件

### Q4: 隧道频繁断开重连

1. 确认 `protocol: http2`（QUIC 在某些网络下不稳定）
2. 确认 `retries: 5` 已配置
3. 检查本地网络是否有限制出站 UDP/7844 端口的防火墙

### Q5: 如何更新 cloudflared

```bash
cloudflared update
```

如果是 Windows 服务模式：
```bash
net stop CloudflaredTunnel
cloudflared update
net start CloudflaredTunnel
```

### Q6: 如何查看隧道状态

```bash
cloudflared tunnel list          # 列出所有隧道
cloudflared tunnel info salary-system  # 查看隧道详情
```

### Q7: 如何卸载隧道服务

```bash
cloudflared service uninstall
```

---

## 文件清单

```
课程设计/
├── cloudflared.exe            # Tunnel 客户端
├── config.yml                 # Tunnel 配置（setup-tunnel.bat 自动生成）
├── cloudflared.log            # Tunnel 运行日志（自动生成）
├── setup-tunnel.bat           # 一键初始化（首次使用）
├── start-all.bat              # 一键启动（后端 + Tunnel）
├── start-tunnel.bat           # 单独启动 Tunnel
├── install-service.bat        # 注册 Windows 服务
└── salary-management/
    ├── backend/
    │   ├── startup.bat        # 后端启动脚本
    │   ├── target/
    │   │   └── salary-management-1.0.0.jar
    │   └── src/main/resources/
    │       ├── static/        # 前端构建产物
    │       │   ├── index.html
    │       │   └── assets/
    │       ├── application.yml
    │       ├── application-dev.yml
    │       └── application-prod.yml
    └── frontend/
        └── dist/              # npm run build 产物
```

---

## Cloudflare Dashboard 推荐设置

登录 [Cloudflare Dashboard](https://dash.cloudflare.com)，选择你的域名：

1. **SSL/TLS** → 加密模式 → **Full (Strict)**
2. **SSL/TLS** → Edge Certificates → Always Use HTTPS → **开启**
3. **Security** → WAF → 托管规则 → **开启**（免费计划包含基础规则）
4. **Security** → Settings → Security Level → **Medium**
5. **Speed** → Optimization → Auto Minify → **全部勾选**
6. **Caching** → Configuration → Browser Cache TTL → **4 hours**
