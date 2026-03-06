# Backend

当前为 TalkHub 后端初始化骨架，包含：

- Spring Boot 基础启动类
- Web / WebSocket / JPA / Redis / RabbitMQ 依赖
- PostgreSQL 环境变量配置
- 基础健康检查接口 `GET /api/health`

## 运行

```bash
./mvnw spring-boot:run
```

## 注意

- 当前只完成初始化，不包含登录、JWT 校验、WebSocket 业务处理
- `spring.jpa.hibernate.ddl-auto=validate`，正式建表前启动会依赖后续 schema 或迁移脚本

