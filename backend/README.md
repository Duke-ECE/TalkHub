# Backend

当前为 TalkHub 后端初始化骨架，包含：

- Spring Boot 基础启动类
- Web / JPA / Redis / RabbitMQ 依赖
- Netty IM 服务端依赖
- PostgreSQL 环境变量配置
- 基础健康检查接口 `GET /api/health`

## 运行

```bash
./mvnw spring-boot:run
```

启用 mock 数据：

```bash
APP_MOCK_ENABLED=true APP_MOCK_PASSWORD=mock123456 ./mvnw spring-boot:run
```

## 注意

- 当前只完成初始化，不包含完整登录、JWT 校验、Netty IM 业务处理
- `spring.jpa.hibernate.ddl-auto=validate`，正式建表前启动会依赖后续 schema 或迁移脚本
- mock 模式会额外初始化 `lea`、`mika`、`sora` 三个用户，以及 `general`、`product`、`ops` 三个频道和示例消息
