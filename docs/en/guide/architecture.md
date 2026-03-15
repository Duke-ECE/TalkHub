# Architecture

## Default Architecture

- Frontend: React SPA (Vite)
- Backend: Spring Boot (REST API) + Netty (IM long-lived connections)
- Cache: Redis (presence, session routing, offline messages, pending ACK state)
- Queue: RabbitMQ (phase-2 async persistence and event broadcast)
- Persistence: PostgreSQL

## Core Domain Model

- User
- Channel
- ChannelMember
- Message
- MessageReadState / UnreadCounter
- OnlinePresence
- ImSession
- MessageDelivery

## Design Focus

- Real-time message ordering and idempotency
- Reconnection and state recovery
- Cache/database consistency strategy
- Unified auth boundary (JWT + Netty connection auth)
