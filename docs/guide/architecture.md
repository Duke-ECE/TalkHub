# 系统架构

## 默认架构

- 前端：React 单页应用（Vite）
- 后端：Spring Boot（REST API）+ Netty（IM 长连接）
- 缓存：Redis（在线状态、会话路由、离线消息、待 ACK）
- 消息队列：RabbitMQ（第二阶段用于异步落库、事件广播）
- 持久化：PostgreSQL

## 核心领域模型

- User
- Channel
- ChannelMember
- Message
- MessageReadState / UnreadCounter
- OnlinePresence
- ImSession
- MessageDelivery

## 设计重点

- 实时消息顺序与幂等处理
- 断线重连与状态恢复
- 缓存与数据库一致性策略
- 鉴权边界统一（JWT + Netty 连接鉴权）
