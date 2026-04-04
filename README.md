# Campus Service Hub

Campus Service Hub is a Spring-based microservices project that demonstrates:

- Centralized routing and JWT validation with `api-gateway`
- Authentication and token lifecycle in `auth-service`
- Campus service request management in `request-service`
- Low-latency downstream provisioning with gRPC in `billing-service`
- Event-driven analytics with Kafka in `analytics-service`

## Architecture

- `api-gateway` (port `4004`)
  - Public entrypoint
  - Routes `/auth/**` to auth service
  - Routes `/api/service-requests/**` to request service
  - Applies JWT validation filter on protected request routes
- `auth-service` (port `4005`)
  - Login and token validation endpoints
  - PostgreSQL-backed user storage
- `request-service` (port `4000`)
  - Handles service request CRUD under `/service-requests`
  - Calls gRPC provisioning endpoint on `billing-service`
  - Publishes request events to Kafka topic `campus.service.requests`
- `billing-service` (ports `4001`, `9001`)
  - Exposes gRPC endpoint for downstream provisioning workflow
- `analytics-service` (port `4002`)
  - Consumes Kafka events from `campus.service.requests`

## Local run (Docker Compose)

1. Copy environment defaults:

```bash
cp .env.example .env
```

2. Build and start the full stack:

```bash
docker compose up --build
```

3. Test login:

```bash
curl -X POST http://localhost:4004/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"testuser@test.com","password":"password123"}'
```

4. Use returned bearer token to query requests:

```bash
curl http://localhost:4004/api/service-requests \
  -H "Authorization: Bearer <TOKEN>"
```

## Service request flow

1. Client sends request to `api-gateway`.
2. Gateway validates JWT by calling `auth-service /validate`.
3. Request is persisted by `request-service`.
4. `request-service` calls `billing-service` over gRPC for provisioning.
5. `request-service` emits lifecycle events (for example, `SERVICE_REQUEST_SUBMITTED`, `SERVICE_REQUEST_PROVISIONED`) to Kafka.
6. `analytics-service` consumes and logs the event.

## API docs

- Request service docs via gateway:
  - `http://localhost:4004/api-docs/requests`
- Auth service docs via gateway:
  - `http://localhost:4004/api-docs/auth`
