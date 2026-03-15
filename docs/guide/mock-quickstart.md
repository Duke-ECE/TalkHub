# Mock 版本快速上手

这份指南针对当前仓库已经实现的 mock 版本，目标是用最短路径把前后端都跑起来，并能直接登录体验。

## 你需要准备什么

- Node.js 20+
- Java 21+
- PostgreSQL 14+

当前最小依赖面：

- 必需：PostgreSQL
- 可暂不准备：Redis、RabbitMQ

原因：

- `make mock` 会向后端灌入演示数据
- 但后端依然需要连接 PostgreSQL，并执行建库和建表初始化
- 当前浏览器演示链路不要求你先接通完整 Netty TCP 客户端

## 1. 初始化环境文件

在项目根目录执行：

```bash
cp .env.example .env
```

如果你的 PostgreSQL 就跑在本机默认端口，通常只要确认这些配置即可：

```env
PG_HOST=localhost
PG_PORT=5432
PG_USER=postgres
PG_PASSWORD=postgres
PG_DATABASE=talkhub
DB_AUTO_CREATE_IF_MISSING=true
```

说明：

- `DB_AUTO_CREATE_IF_MISSING=true` 时，后端会尝试自动创建目标数据库
- 如果你已经自己建好了库，也可以继续保留这个配置

## 2. 一条命令启动 mock 版本

```bash
make mock
```

这个命令会同时：

- 启动后端 Spring Boot 服务
- 启动前端 Vite 开发服务器
- 灌入 mock 用户、频道和示例消息
- 自动给前端设置本地 API 地址

## 3. 访问地址

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`
- 浏览器实时桥接：`http://localhost:8080/api/im/browser/events`

如果 `8080` 被占用，`make mock` 会自动顺延端口，前端也会跟着切换到新的后端地址。

## 4. 默认登录账号

- `admin / admin123456`
- `lea / mock123456`
- `mika / mock123456`
- `sora / mock123456`

其中：

- `admin` 是系统初始化管理员
- `lea`、`mika`、`sora` 是 mock 模式额外灌入的演示用户

## 5. 你现在能验证什么

跑起来后，当前版本适合优先验证这些能力：

- 登录
- JWT 持久化
- 频道历史消息查询
- 浏览器调试发送消息
- 浏览器在线用户和实时消息桥接

## 6. 当前版本的边界

为了避免预期错位，建议先按下面的理解使用这个版本：

- 浏览器端当前不是直接连接 Netty TCP 自定义协议
- 浏览器端走的是 Spring 提供的 `/api/im/browser` 适配层
- 真正的 Netty 长连接主链路更适合给 IM 客户端或后续网关接入

## 7. 常见问题

## 启动时报数据库连接失败

先检查：

- PostgreSQL 是否已经启动
- `.env` 里的 `PG_HOST`、`PG_PORT`、`PG_USER`、`PG_PASSWORD` 是否正确
- 当前用户是否有创建数据库权限

## 前端打不开或接口 404

先看：

- `make mock` 终端里后端是否已经成功启动
- `SERVER_PORT` 是否因为端口冲突被自动切换

## 想只起后端 mock

```bash
cd backend
APP_MOCK_ENABLED=true APP_MOCK_PASSWORD=mock123456 ./mvnw spring-boot:run
```

## 想只起前端

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 \
VITE_IM_GATEWAY_URL=http://localhost:8080/api/im/browser \
npm run dev
```
