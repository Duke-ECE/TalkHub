SHELL := /bin/bash

MAVEN_REPO_LOCAL ?= /tmp/m2
MVNW := ./mvnw -Dmaven.repo.local=$(MAVEN_REPO_LOCAL)

.PHONY: help ensure-env ensure-m2 ensure-frontend-deps install backend-install frontend-install build backend-build frontend-build backend-test frontend-test test run mock run-mock docs-install docs-run docs-build clean

help:
	@printf "%s\n" \
		"make install  - copy .env from .env.example if missing, install deps, build backend and frontend" \
		"make test     - run backend tests and frontend checks" \
		"make run      - start backend and frontend together with variables from root .env" \
		"make mock     - start backend and frontend with backend mock users/channels/messages enabled" \
		"make docs-install - install docs dependencies" \
		"make docs-run     - start VitePress docs dev server on 5174" \
		"make docs-build   - build VitePress docs site" \
		"make clean    - remove frontend dist and backend target"

ensure-env:
	@if [ ! -f .env ]; then \
		cp .env.example .env; \
		echo "Created .env from .env.example. Update it before connecting to real services."; \
	fi

ensure-m2:
	@mkdir -p $(MAVEN_REPO_LOCAL)

ensure-frontend-deps:
	@cd frontend; \
	if [ ! -x node_modules/.bin/tsc ] || [ ! -x node_modules/.bin/vite ]; then \
		rm -rf node_modules; \
		npm ci; \
	fi

install: ensure-env ensure-m2 backend-install frontend-install frontend-build

backend-install: ensure-m2
	@set -a; source ./.env; \
	if [ -z "$${DB_DSN}" ]; then \
		export DB_DSN="jdbc:postgresql://$${PG_HOST:-localhost}:$${PG_PORT:-5432}/$${PG_DATABASE:-echocenter}?sslmode=$${PG_SSLMODE:-disable}"; \
	fi; \
	set +a; \
	cd backend && $(MVNW) -q -DskipTests package

frontend-install: ensure-frontend-deps

build: backend-build frontend-build

backend-build: ensure-m2
	@set -a; source ./.env; \
	if [ -z "$${DB_DSN}" ]; then \
		export DB_DSN="jdbc:postgresql://$${PG_HOST:-localhost}:$${PG_PORT:-5432}/$${PG_DATABASE:-echocenter}?sslmode=$${PG_SSLMODE:-disable}"; \
	fi; \
	set +a; \
	cd backend && $(MVNW) -q -DskipTests package

frontend-build: ensure-frontend-deps
	@set -a; source ./.env; set +a; \
	cd frontend && npm run build

backend-test: ensure-m2
	@cd backend && $(MVNW) -B test

frontend-test: ensure-frontend-deps
	@cd frontend && npm run build

test: backend-test frontend-test

run: ensure-env ensure-m2 ensure-frontend-deps
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
	(cd backend && $(MVNW) spring-boot:run) & \
	(cd frontend && npm run dev -- --host 0.0.0.0) & \
	wait

mock: run-mock

run-mock: ensure-env ensure-m2 ensure-frontend-deps
	@set -a; source ./.env; \
	export APP_MOCK_ENABLED=true; \
	export APP_MOCK_PASSWORD="$${APP_MOCK_PASSWORD:-mock123456}"; \
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
	if [ -z "$${VITE_IM_GATEWAY_URL}" ]; then \
		export VITE_IM_GATEWAY_URL="http://localhost:$${SERVER_PORT}/api/im/browser"; \
	fi; \
	set +a; \
	echo "Mock users: admin / $${ADMIN_PASSWORD:-admin123456}, lea / $${APP_MOCK_PASSWORD}, mika / $${APP_MOCK_PASSWORD}, sora / $${APP_MOCK_PASSWORD}"; \
	trap 'kill 0' INT TERM EXIT; \
	(cd backend && $(MVNW) spring-boot:run) & \
	(cd frontend && npm run dev -- --host 0.0.0.0) & \
	wait

docs-install:
	@cd docs && npm install

docs-run:
	@cd docs && npm run docs:dev

docs-build:
	@cd docs && npm run docs:build

clean:
	@rm -rf backend/target frontend/dist docs/.vitepress/dist
