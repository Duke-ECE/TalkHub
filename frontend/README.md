# Frontend

当前为 TalkHub 前端初始化骨架，包含：

- React + Vite + TypeScript
- Tailwind CSS
- 登录页 + 频道工作台
- 历史消息查询
- API/IM 适配层环境变量入口

## 运行

```bash
npm install
npm run dev
```

## 环境变量

- `VITE_API_BASE_URL`
- `VITE_IM_GATEWAY_URL`
- `VITE_IM_TRANSPORT_HINT`

## 说明

- 当前后端实时链路是 Netty TCP 自定义协议，浏览器不能直接连接
- 前端已经接好登录、历史消息 REST 流程和浏览器 SSE 适配层
- 浏览器通过 `GET /api/im/browser/events` 订阅实时消息与在线态
- 浏览器通过 `POST /api/im/browser/channels/{channelId}/messages` 把消息交给 Spring 适配层，再桥接到 Netty 广播链路
