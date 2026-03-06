# 系统架构

## 默认架构

- 前端：React 单页应用（Vite）
- 后端：Spring Boot（REST API + WebSocket）
- 缓存：Redis（在线状态、成员态、未读计数）
- 消息队列：RabbitMQ（异步落库、事件广播）
- 持久化：PostgreSQL

## 核心领域模型

- User
- Channel
- ChannelMember
- Message
- MessageReadState / UnreadCounter
- OnlinePresence

## 设计重点

- 实时消息顺序与幂等处理
- 断线重连与状态恢复
- 缓存与数据库一致性策略
- 鉴权边界统一（JWT + WebSocket 连接鉴权）

