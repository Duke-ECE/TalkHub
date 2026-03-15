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
- 前端已经接好登录和历史消息 REST 流程
- 前端当前通过 `POST /api/channels/{channelId}/messages` 打通浏览器发送闭环
- 实时消息接收仍需要额外的网关适配层或 HTTP 调试接口
