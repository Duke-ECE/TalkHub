# TalkHub

TalkHub is a lightweight real-time chat and voice platform.

## Overview

- Frontend: React + Vite + Tailwind CSS
- Backend: Spring Boot + WebSocket
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

Default local endpoints:

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/api/health`

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

## Environment Variables

- Template: `.env.example`
- Local file: `.env` (ignored by Git)

The project uses PostgreSQL settings from env vars by default. You can also provide a full `DB_DSN`.

## License

This project is licensed under the [MIT License](./LICENSE).

---

## 中文说明

TalkHub 是一个轻量级实时聊天与语音平台。

### 技术栈

- 前端：React + Vite + Tailwind CSS
- 后端：Spring Boot + WebSocket
- 缓存：Redis
- 消息队列：RabbitMQ
- 数据库：PostgreSQL

### 快速启动

```bash
make install
make run
```

默认访问地址：

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`

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

### 许可证

本项目使用 [MIT License](./LICENSE)。
