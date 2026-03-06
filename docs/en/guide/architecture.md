# Architecture

## Default Architecture

- Frontend: React SPA (Vite)
- Backend: Spring Boot (REST API + WebSocket)
- Cache: Redis (presence, membership, unread counters)
- Queue: RabbitMQ (async persistence, event broadcast)
- Persistence: PostgreSQL

## Core Domain Model

- User
- Channel
- ChannelMember
- Message
- MessageReadState / UnreadCounter
- OnlinePresence

## Design Focus

- Real-time message ordering and idempotency
- Reconnection and state recovery
- Cache/database consistency strategy
- Unified auth boundary (JWT + WebSocket auth)

