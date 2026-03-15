# Getting Started

If your goal is to get the current login-ready demo running quickly, start with [Mock Quickstart](/en/guide/mock-quickstart).

## Requirements

- Node.js 20+
- Java 21+ (the project currently also runs on Java 25)
- PostgreSQL 14+

## Install Dependencies

Run in project root:

```bash
make install
```

## Run Frontend + Backend

```bash
make run
```

To start the version with mock accounts and seeded demo data:

```bash
make mock
```

Default endpoints:

- Frontend: `http://localhost:5173`
- Backend health: `http://localhost:8080/api/health`

## Run Docs Site

```bash
cd docs
npm install
npm run docs:dev
```

Default docs URL: `http://localhost:5174`
