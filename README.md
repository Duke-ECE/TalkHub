# TalkHub

TalkHub 是一个轻量级实时聊天与语音平台。本仓库当前已完成第一阶段第 1 步“项目初始化”的基础骨架。

## 目录

- `backend`: Spring Boot 后端
- `frontend`: React + Vite + Tailwind 前端
- `shared`: 共享协议、常量、示例数据预留目录
- `docs`: 项目文档
- `specs`: 接口与协议草案预留目录

## 环境变量

复制根目录 `.env.example` 后按实际环境填写。数据库统一按 PostgreSQL 配置。

## 启动

### 一键安装与运行

```bash
make install
make run
```

`make install` 会在根目录不存在 `.env` 时，自动用 `.env.example` 复制出一份 `.env`，然后安装并编译前后端。

`make run` 会读取根目录 `.env`，并同时启动后端和前端开发服务。

### 手动启动

```bash
cd backend
./mvnw spring-boot:run
```

```bash
cd frontend
npm install
npm run dev
```

## 当前状态

已完成：

- 项目目录初始化
- Spring Boot 最小后端骨架
- React + Vite + Tailwind 最小前端骨架
- PostgreSQL 环境变量模板

未完成：

- 登录、JWT、WebSocket、聊天、持久化等业务能力
