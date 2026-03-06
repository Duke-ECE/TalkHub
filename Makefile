SHELL := /bin/bash

.PHONY: help ensure-env install backend-install frontend-install build backend-build frontend-build run clean

help:
	@printf "%s\n" \
		"make install  - copy .env from .env.example if missing, install deps, build backend and frontend" \
		"make run      - start backend and frontend together with variables from root .env" \
		"make clean    - remove frontend dist and backend target"

ensure-env:
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo "Created .env from .env.example. Update it before connecting to real services."; \
	fi

install: ensure-env backend-install frontend-install frontend-build

backend-install:
	@set -a; source ./.env; \
	if [ -z "$${DB_DSN}" ]; then \
		export DB_DSN="jdbc:postgresql://$${PG_HOST:-localhost}:$${PG_PORT:-5432}/$${PG_DATABASE:-echocenter}?sslmode=$${PG_SSLMODE:-disable}"; \
	fi; \
	set +a; \
	cd backend && ./mvnw -q -DskipTests package

frontend-install:
	@cd frontend && npm install

build: backend-build frontend-build

backend-build:
	@set -a; source ./.env; \
	if [ -z "$${DB_DSN}" ]; then \
		export DB_DSN="jdbc:postgresql://$${PG_HOST:-localhost}:$${PG_PORT:-5432}/$${PG_DATABASE:-echocenter}?sslmode=$${PG_SSLMODE:-disable}"; \
	fi; \
	set +a; \
	cd backend && ./mvnw -q -DskipTests package

frontend-build:
	@set -a; source ./.env; set +a; \
	cd frontend && npm run build

run: ensure-env
	@set -a; source ./.env; \
	if [ -z "$${DB_DSN}" ]; then \
		export DB_DSN="jdbc:postgresql://$${PG_HOST:-localhost}:$${PG_PORT:-5432}/$${PG_DATABASE:-echocenter}?sslmode=$${PG_SSLMODE:-disable}"; \
	fi; \
	export SERVER_PORT="$${SERVER_PORT:-8080}"; \
	ORIGINAL_SERVER_PORT="$${SERVER_PORT}"; \
	while lsof -iTCP:$${SERVER_PORT} -sTCP:LISTEN -t >/dev/null 2>&1; do \
		SERVER_PORT=$$((SERVER_PORT + 1)); \
	done; \
	if [ "$${SERVER_PORT}" != "$${ORIGINAL_SERVER_PORT}" ]; then \
		echo "Port $${ORIGINAL_SERVER_PORT} is in use, switching backend to $${SERVER_PORT}"; \
	fi; \
	if [ -z "$${VITE_API_BASE_URL}" ] || [ "$${VITE_API_BASE_URL}" = "http://localhost:8080" ]; then \
		export VITE_API_BASE_URL="http://localhost:$${SERVER_PORT}"; \
	fi; \
	if [ -z "$${VITE_WS_URL}" ] || [ "$${VITE_WS_URL}" = "ws://localhost:8080/ws" ]; then \
		export VITE_WS_URL="ws://localhost:$${SERVER_PORT}/ws"; \
	fi; \
	set +a; \
	trap 'kill 0' INT TERM EXIT; \
	(cd backend && ./mvnw spring-boot:run) & \
	(cd frontend && npm run dev -- --host 0.0.0.0) & \
	wait

clean:
	@rm -rf backend/target frontend/dist
