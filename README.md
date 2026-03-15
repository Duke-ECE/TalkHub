# TalkHub

TalkHub is a high-performance instant messaging and chat platform.

## Overview

- Frontend: React + Vite + Tailwind CSS
- Backend: Spring Boot + Netty
- Cache: Redis
- Queue: RabbitMQ
- Database: PostgreSQL

## Repository Structure

- `backend`: Spring Boot backend service
- `frontend`: React + Vite web client
- `docs`: VitePress documentation site (English + Chinese)
- `shared`: shared protocol/constants placeholders
- `specs`: requirement/design specifications

## Quick Start

### 1) Install dependencies

```bash
make install
```

### 2) Run backend + frontend

```bash
make run
```

Run with backend mock data:

```bash
make mock
```

Default local endpoints:

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/api/health`
- Channel history: `GET /api/channels/1/messages`
- HTTP debug send: `POST /api/channels/1/messages`
- Browser realtime bridge: `GET /api/im/browser/events`

### 3) Run all tests (backend + frontend)

```bash
make test
```

## Documentation Site (VitePress)

### Local docs development

```bash
make docs-install
make docs-run
```

Docs site URL: `http://localhost:5174`

### Build docs

```bash
make docs-build
```

## CI/CD: Auto Deploy Docs to GitHub Pages

This repository includes a GitHub Actions workflow:

- `.github/workflows/deploy-docs.yml`

Behavior:

- Trigger: every push to `main` (and manual `workflow_dispatch`)
- Build: `docs` with VitePress
- Deploy: publish `docs/.vitepress/dist` to GitHub Pages

Notes:

- Workflow automatically sets VitePress `base`:
  - `"/"` for `<user>.github.io` repositories
  - `"/<repo-name>/"` for normal project repositories
- Ensure GitHub Pages source is set to **GitHub Actions** in repository settings.

## CI: Auto Test Frontend + Backend (All Branches)

This repository includes a CI workflow:

- `.github/workflows/ci.yml`

Behavior:

- Trigger: every `push` to any branch and every `pull_request` to any branch
- CI job runs unified command: `make test`

## Environment Variables

- Template: `.env.example`
- Local file: `.env` (ignored by Git)

The project uses PostgreSQL settings from env vars by default. You can also provide a full `DB_DSN`.

## License

This project is licensed under the [MIT License](./LICENSE).

---

## 中文说明

TalkHub 是一个面向高性能即时通信场景的聊天平台。

### 技术栈

- 前端：React + Vite + Tailwind CSS
- 后端：Spring Boot + Netty
- 缓存：Redis
- 消息队列：RabbitMQ
- 数据库：PostgreSQL

### 快速启动

```bash
make install
make run
```

如果想直接带后端 mock 数据启动：

```bash
make mock
```

默认访问地址：

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`
- 频道历史消息：`GET /api/channels/1/messages`
- 浏览器调试发送：`POST /api/channels/1/messages`

`make mock` 会额外灌入：

- mock 用户：`lea`、`mika`、`sora`
- mock 频道：`general`、`product`、`ops`
- mock 用户默认密码：`mock123456`

### 当前前端链路说明

- 浏览器端已经支持：登录、JWT 持久化、历史消息查询、HTTP 调试发送
- 后端即时通信主链路仍然是 Netty TCP 自定义协议
- 如果要在浏览器中接入真正的实时收发，需要单独增加 IM 网关或适配层

### 测试（前后端一起）

```bash
make test
```

### 文档站（VitePress）

```bash
make docs-install
make docs-run
```

默认地址：`http://localhost:5174`

构建文档：

```bash
make docs-build
```

### 文档自动发布（GitHub Pages）

- 工作流文件：`.github/workflows/deploy-docs.yml`
- 触发条件：`push` 到 `main`
- 发布内容：`docs/.vitepress/dist`
- Pages 来源请设置为：**GitHub Actions**

### 全分支自动测试（前后端 CI）

- 工作流文件：`.github/workflows/ci.yml`
- 触发条件：所有分支的 `push` 和 `pull_request`
- CI 统一执行：`make test`

### 许可证

本项目使用 [MIT License](./LICENSE)。
