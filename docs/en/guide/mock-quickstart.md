# Mock Quickstart

This guide is for the current mock-enabled version of the repository. The goal is to get both frontend and backend running quickly and log in immediately.

## What You Need

- Node.js 20+
- Java 21+
- PostgreSQL 14+

Current minimum dependency surface:

- Required: PostgreSQL
- Optional for basic local onboarding: Redis, RabbitMQ

Why:

- `make mock` seeds demo data into the backend
- the backend still needs PostgreSQL for database bootstrap and schema initialization
- the current browser demo flow does not require a full Netty TCP client setup first

## 1. Initialize the env file

Run in the project root:

```bash
cp .env.example .env
```

If your PostgreSQL runs locally on the default port, these are the main values to verify:

```env
PG_HOST=localhost
PG_PORT=5432
PG_USER=postgres
PG_PASSWORD=postgres
PG_DATABASE=talkhub
DB_AUTO_CREATE_IF_MISSING=true
```

Notes:

- when `DB_AUTO_CREATE_IF_MISSING=true`, the backend will try to create the target database automatically
- if you already created the database yourself, you can still keep this enabled

## 2. Start the mock version

```bash
make mock
```

This command will:

- start the Spring Boot backend
- start the Vite frontend
- seed mock users, channels, and demo messages
- point the frontend to the local backend automatically

## 3. Local URLs

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/api/health`
- Browser realtime bridge: `http://localhost:8080/api/im/browser/events`

If port `8080` is already in use, `make mock` will move to the next free port and update the frontend target automatically.

## 4. Default Login Accounts

- `admin / admin123456`
- `lea / mock123456`
- `mika / mock123456`
- `sora / mock123456`

Where:

- `admin` is the built-in administrator account
- `lea`, `mika`, and `sora` are seeded only when mock mode is enabled

## 5. What You Can Verify Right Now

After startup, the current version is best suited for validating:

- login
- JWT persistence
- channel history loading
- browser debug message sending
- browser online user and realtime bridge flow

## 6. Current Scope Boundaries

To avoid confusion, use the current version with these expectations:

- the browser is not directly connected to the Netty TCP custom protocol
- the browser uses the Spring `/api/im/browser` adapter layer
- the Netty long-connection main path is better treated as the IM-side transport foundation

## 7. Common Issues

## Database connection failed on startup

Check:

- whether PostgreSQL is running
- whether `PG_HOST`, `PG_PORT`, `PG_USER`, and `PG_PASSWORD` in `.env` are correct
- whether your PostgreSQL user can create databases

## Frontend opens but APIs fail

Check:

- whether the backend in the `make mock` terminal actually finished startup
- whether `SERVER_PORT` was auto-shifted because `8080` was occupied

## Start mock backend only

```bash
cd backend
APP_MOCK_ENABLED=true APP_MOCK_PASSWORD=mock123456 ./mvnw spring-boot:run
```

## Start frontend only

```bash
cd frontend
VITE_API_BASE_URL=http://localhost:8080 \
VITE_IM_GATEWAY_URL=http://localhost:8080/api/im/browser \
npm run dev
```
