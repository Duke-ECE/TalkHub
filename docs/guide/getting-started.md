# 快速开始

## 环境要求

- Node.js 20+
- Java 21+（项目当前也可在 Java 25 启动）
- PostgreSQL 14+

## 安装依赖

在项目根目录执行：

```bash
make install
```

## 启动前后端

```bash
make run
```

默认访问地址：

- 前端：`http://localhost:5173`
- 后端健康检查：`http://localhost:8080/api/health`

## 启动文档站

```bash
cd docs
npm install
npm run docs:dev
```

默认访问地址：`http://localhost:5174`

