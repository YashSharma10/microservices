# Carbon Credit Microservices — Developer Guide

This document describes the Carbon Credit microservices project, how the services are organized, how data flows between them, how to run the system locally (development), and recommended fixes/improvements.

It was created by scanning the repository code and wiring the API Gateway, Configuration Server, and Service Discovery modules to match the patterns used in the `college` sample project.

## Table of contents

- Project overview
- Architecture and components
- Services and their HTTP APIs
- Data model (tables)
- Configuration & config server
- How to run locally (PowerShell commands)
- Example API calls
- Known issues & recommended fixes
- Troubleshooting
- Next steps

---

## Project overview

This repository implements a small microservices system to manage and trade "carbon credits". The system is split into multiple independent services:

- `configurationserver` — Spring Cloud Config Server (serves application config from native files or a Git repo).
- `servicediscovery` — Eureka service discovery server.
- `apigateway` — Spring Cloud Gateway; routes requests and applies global authentication filter.
- `authservice` — Authentication service (signup/login) and JWT generation.
- `userservice` — Manages user accounts and credit balances.
- `carbonservice` — Catalog of carbon assets (create/read/update/delete).
- `tradeservice` — Performs trades (transfer credits between users and persist trade records).

Each service has its own module under `carboncredit/` and its own DB schema file where applicable.

## Architecture (how components fit together)

Client -> API Gateway -> (via Eureka discovery) => backend services

- The `apigateway` routes client HTTP requests to backend services using `lb://<service>` URIs (Eureka client resolves them).
- `configurationserver` provides centralized config (port, credentials, secrets) to services.
- `servicediscovery` (Eureka) is the registry where services register themselves and discover others.

Simple ASCII diagram:

```
  +--------+       +-------------+       +----------------+
  | Client |  -->  | API Gateway |  -->  |  Backend SRV   |
  +--------+       +-------------+       +----------------+
                         |  ^                   ^   ^   ^
                         |  |                   |   |   |
                         v  |                   |   |   |
                  +----------------+            |   |   |
                  | Service Discovery | <--------+   |   |
                  +----------------+                |   |
                            ^                        |   |
                            |                        |   |
                   +-------------------+             |   |
                   | ConfigurationServer|-------------+   +---> Config for each service
                   +-------------------+
```

## Services & endpoints (summary)

Below is a concise reference for each service and its primary endpoints. Use these paths when calling the gateway; the gateway routes to the services using the `GatewayConfig` mapping.

Note: The controller-level mappings appear in the code under each module (e.g., `userservice/src/main/java/.../UserController.java`).

### `authservice` (base path `/auth`)

- POST `/auth/signup`
  - Body: `{ "username": "...", "password": "..." }`
  - Creates a user in the auth DB. Returns 200 OK on success, 409 if user exists.

- POST `/auth/login`
  - Body: `{ "username": "...", "password": "..." }`
  - Returns `{ "token": "<jwt>" }` on success.

Internal details:
- Uses `JdbcUserRepository` storing users in a local database table `users`.
- Generates JWT tokens using `JwtUtil` and protects endpoints with `JwtFilter`.

### `userservice` (base path `/users`)

- POST `/users` — create user (UserDto)
- GET `/users/{id}` — get user
- POST `/users/{id}/addCredits?amount=<double>` — add credits
- POST `/users/{id}/removeCredits?amount=<double>` — remove credits

Model: `User { id, name, credits }`.

### `carbonservice` (base path `/carbon`)

- GET `/carbon` — list assets
- GET `/carbon/{id}` — get asset
- POST `/carbon` — create
- PUT `/carbon/{id}` — update
- DELETE `/carbon/{id}` — delete

Model: `Carbon { id, name, supply }`.

### `tradeservice` (base path `/trade`)

- POST `/trade` — initiate a trade
  - Body: `{ "from": <userId>, "to": <userId>, "amount": <double> }`
  - Flow: calls `userservice` to remove credits from `from` then add to `to`, and saves a `Trade` record.
- GET `/trade` — list trades
- GET `/trade/{id}` — get trade

Model: `Trade { id, fromUserId, toUserId, amount, createdAt }`.

### `apigateway`

- Routes configured in `GatewayConfig` map public paths to service names (via `lb://<service>`). The code routes:
  - `/carbon/**` -> `carbonservice`
  - `/trades/**` -> `tradeservice` (note: controller uses `/trade` — align these)
  - `/users/**` -> `userservice`
  - `/auth/**` -> `authservice`

- `AuthGlobalFilter` requires non-`/auth/` requests to present Basic auth (this filter currently expects an `authenticate` endpoint in `authservice` that does not exist; see "Known issues").

## Database tables (schema.sql files)

- `authservice/src/main/resources/schema.sql` — `users(id, username, password)`
- `userservice/src/main/resources/schema.sql` — `users(id, name, credits)`
- `tradeservice/src/main/resources/schema.sql` — `trades(id, from_user_id, to_user_id, amount, created_at)`
- `carbonservice/src/main/resources/schema.sql` — `carbon(id, name, supply)`

Each service manages its own database (microservice-per-database pattern).

## Configuration

- The `configurationserver` module (port 8888) acts as a central configuration server. It can run in two modes:
  - `native` — uses files in `configurationserver/src/main/resources/config/*` (good for local dev).
  - `git` — pulls configuration from a remote Git repository (set in `application.properties`).

- Each service reads configuration from the config server (it is wired in the modules to import config).

## How to run locally (recommended order)

Use PowerShell (the project was verified on Windows). Run config server in `native` mode to use the local config files we added. Then start Eureka, backend services, and finally the gateway.

1) Start Configuration Server (native mode)

```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\configurationserver'
$env:CONFIG_MODE='native'; mvn spring-boot:run
```

2) Start Service Discovery (Eureka)

```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\servicediscovery'
mvn spring-boot:run
```

3) Start backend services (start these in separate shells):

- Auth service
```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\authservice'
mvn spring-boot:run
```

- User service
```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\userservice'
mvn spring-boot:run
```

- Carbon service
```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\carbonservice'
mvn spring-boot:run
```

- Trade service
```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\tradeservice'
mvn spring-boot:run
```

4) Start API Gateway

```powershell
cd 'c:\Users\yashs\Downloads\class13\carboncredit\apigateway'
mvn spring-boot:run
```

Notes:
- Some services may use `server.port=0` in native config which picks a random port; Eureka will register the selected port.
- If a service doesn't appear in Eureka, check its logs for config errors or port conflicts.

## Example API calls

Use the gateway's port (default 8080 if configured) to call services. The examples below assume gateway is at `http://localhost:8080`.

1. Signup and login (auth)

```powershell
# Signup
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/signup' -Body (ConvertTo-Json @{ username='alice'; password='pass' }) -ContentType 'application/json'

# Login -> returns a token
$login = Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/auth/login' -Body (ConvertTo-Json @{ username='alice'; password='pass' }) -ContentType 'application/json'
$token = $login.token
```

2. Create users

```powershell
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/users' -Body (ConvertTo-Json @{ name='Alice'; credits=100.0 }) -ContentType 'application/json'
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/users' -Body (ConvertTo-Json @{ name='Bob'; credits=10.0 }) -ContentType 'application/json'
```

3. Execute a trade

```powershell
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/trade' -Body (ConvertTo-Json @{ from=1; to=2; amount=5.0 }) -ContentType 'application/json'
```

4. Create a carbon asset

```powershell
Invoke-RestMethod -Method Post -Uri 'http://localhost:8080/carbon' -Body (ConvertTo-Json @{ name='CarbonX'; supply=1000 }) -ContentType 'application/json'
```

## Known issues & recommended fixes

I found a few mismatches inherited from merging patterns between the `college` project and this `carboncredit` project. Below are problems you should resolve for a robust system.

1. Authentication flow inconsistency
   - Problem: The gateway's `AuthGlobalFilter` expects to validate Basic auth by calling `lb://authservice/auth/authenticate`, but `authservice` currently exposes only `/auth/signup` and `/auth/login` (login returns JWT). That means gateway filter will fail.
   - Fix options:
     - Option A (quick): Implement `/auth/authenticate` in `authservice` that accepts Basic-style credentials and returns 200 on success.
     - Option B (recommended): Use JWT across the board. Let clients call `/auth/login` to obtain JWT and modify the gateway to accept/validate Bearer tokens (or pass them through to downstream services).

2. Tradeservice calls userservice with a hard-coded URL
   - Problem: `TradeService` uses `http://localhost:8082/users` instead of using Eureka (`lb://userservice`) or a load-balanced RestTemplate/WebClient.
   - Fix: Inject a `@LoadBalanced RestTemplate` or `WebClient` and use `http://userservice/users/...` so Eureka resolves the host/port.

3. Gateway route path mismatch
   - Problem: gateway route uses `/trades/**` while `tradeservice` controller maps `/trade` (singular). Align them.

4. Secrets in local properties
   - Problem: sample credentials/secrets are stored in `application.properties` for dev. Move them to the config repo or env vars for production.

## Troubleshooting

- If a module fails to start: check logs for errors, then verify `configurationserver` is reachable (or run config server in `native`), and verify the service's `spring.application.name` is correct.
- If services don't show up in Eureka: ensure `servicediscovery` is running and that services are configured with `eureka.client.service-url.defaultZone` pointing to it.
- For port conflicts: check `server.port` in the effective configuration (config server or local `application.properties`).

## Next steps and improvements

- Choose and implement a single authentication strategy (gateway-central or JWT).
- Replace hard-coded service URLs with discovery-aware clients.
- Add health checks (Spring Boot Actuator) and expose relevant endpoints.
- Add integration tests and a small CI pipeline to bring up services in native mode and run smoke tests.
- Dockerize services and provide `docker-compose` for local multi-container development.

---

If you want, I can:

- Implement the recommended auth fix (add `/auth/authenticate` or update gateway to JWT). 
- Replace the trade service hard-coded URL with a discovery-enabled RestTemplate/WebClient.
- Create a `README.md` at the repository root summarizing the whole workspace and adding quick start scripts.

Tell me which of the above you'd like me to implement next and I'll continue.

## Detailed database schemas (complete)

Below are the exact table definitions used across services (from the included `schema.sql` files). These are the source of truth for data stored by each microservice.

1) authservice (table `users`)

```sql
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  username VARCHAR(100) NOT NULL UNIQUE,
  password VARCHAR(200) NOT NULL
);
```

- Columns:
  - `id` (BIGINT) — primary key, auto-increment.
  - `username` (VARCHAR) — unique user login id.
  - `password` (VARCHAR) — hashed password (BCrypt) stored by the service.

2) userservice (table `users`)

```sql
CREATE TABLE IF NOT EXISTS users (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(200) NOT NULL,
  credits DOUBLE DEFAULT 0
);
```

- Columns:
  - `id` (BIGINT) — primary key.
  - `name` (VARCHAR) — display name for the account.
  - `credits` (DOUBLE) — current credit balance.

3) tradeservice (table `trades`)

```sql
CREATE TABLE IF NOT EXISTS trades (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  from_user_id BIGINT NOT NULL,
  to_user_id BIGINT NOT NULL,
  amount DOUBLE NOT NULL,
  created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);
```

- Columns:
  - `id` (BIGINT) — primary key.
  - `from_user_id` (BIGINT) — foreign key (logical) referencing a user id in userservice.
  - `to_user_id` (BIGINT) — destination user id.
  - `amount` (DOUBLE) — amount of credits transferred.
  - `created_at` (TIMESTAMP) — timestamp when the trade was recorded.

4) carbonservice (table `carbon`)

```sql
CREATE TABLE IF NOT EXISTS carbon (
  id BIGINT AUTO_INCREMENT PRIMARY KEY,
  name VARCHAR(150) NOT NULL,
  supply DOUBLE NOT NULL
);
```

- Columns:
  - `id` (BIGINT) — primary key.
  - `name` (VARCHAR) — asset name.
  - `supply` (DOUBLE) — available supply of the asset.

Notes:
- Each service owns its database and schema; there is no shared relational DB across services.
- `from_user_id`/`to_user_id` are logical references only; there are no enforced cross-service foreign keys.

## Complete API reference (full endpoints, request/response shapes)

This section lists every public controller endpoint present in the codebase and the expected request and response payload shapes.

General note: All endpoints below are shown with the path as exposed by the service controllers. When calling via the API Gateway, use the gateway path prefixes (gateway routes map `/auth/**`, `/users/**`, `/carbon/**`, `/trade` etc. to the underlying services via Eureka).

1) authservice

- POST /auth/signup
  - Request body: JSON
    {
      "username": "string",
      "password": "string"
    }
  - Responses:
    - 200 OK — body: "ok"
    - 400 Bad Request — body: "missing" (if required fields missing)
    - 409 Conflict — body: "exists" (username already exists)

- POST /auth/login
  - Request body: JSON
    {
      "username": "string",
      "password": "string"
    }
  - Responses:
    - 200 OK — body: { "token": "<jwt>" }
    - 400 Bad Request — missing fields
    - 401 Unauthorized — invalid credentials

2) userservice

- POST /users
  - Request body: JSON UserDto
    { "name": "string", "credits": number }
  - Response: 200 OK -> User (model returned)

- GET /users/{id}
  - Response: 200 OK -> User JSON, or 404 Not Found

- POST /users/{id}/addCredits?amount=<double>
  - No body required (amount sent as query param)
  - Response: 200 OK -> updated User; 404 Not Found if user missing

- POST /users/{id}/removeCredits?amount=<double>
  - Response: 200 OK -> updated User; 400 Bad Request if insufficient balance; 404 if user missing

User model (example JSON):
```json
{
  "id": 1,
  "name": "Alice",
  "credits": 100.0
}
```

3) carbonservice

- GET /carbon
  - Response: 200 OK -> [CarbonDto]

- GET /carbon/{id}
  - Response: 200 OK -> CarbonDto or 404

- POST /carbon
  - Request: CarbonDto { "name": string, "supply": number }
  - Response: 201 Created, Location header: /carbon/{id}, body: created CarbonDto

- PUT /carbon/{id}
  - Request: CarbonDto
  - Response: 204 No Content or 404 Not Found

- DELETE /carbon/{id}
  - Response: 204 No Content or 404 Not Found

CarbonDto example:
```json
{ "id": 1, "name": "CarbonX", "supply": 1000 }
```

4) tradeservice

- POST /trade
  - Request: TradeDto { "from": <userId>, "to": <userId>, "amount": number }
  - Response:
    - 200 OK -> created Trade JSON if success
    - 400 Bad Request -> when trade fails (insufficient funds or downstream error)
  - Behavior: calls userservice to remove and add credits before persisting trade.

- GET /trade
  - Response: 200 OK -> [Trade]

- GET /trade/{id}
  - Response: 200 OK -> Trade or 404

Trade JSON example:
```json
{
  "id": 10,
  "fromUserId": 1,
  "toUserId": 2,
  "amount": 5.0,
  "createdAt": "2025-11-04T12:34:56"
}
```

5) apigateway (routes & behavior)

- Gateway routes (in code):
  - `/carbon/**` -> lb://carbonservice
  - `/trades/**` -> lb://tradeservice  (note: service controller uses `/trade`)
  - `/users/**` -> lb://userservice
  - `/auth/**` -> lb://authservice

- Global filter behavior (current code):
  - For requests not under `/auth/`:
    1. Expect client to send Basic auth header. If missing -> 401.
    2. Decode Basic and call `lb://authservice/auth/authenticate` with credentials (this endpoint is not currently implemented in `authservice`).
    3. If authenticate OK -> gateway replaces Authorization header with service-specific Basic credentials and adds `X-API-GATEWAY-SECRET` header before forwarding to downstream service.

Important: The gateway's filter and `authservice` must be aligned (either implement `/auth/authenticate` or change gateway to accept JWTs).

## Complete end-to-end user flow (detailed sequence)

Below is a step-by-step flow for a typical user interaction: sign up, login, create accounts, and perform a trade. Each step shows the request, which service handles it, the internal actions and the DB changes.

Assumptions for the flow
- Gateway listening at `http://localhost:8080`.
- Config server running in `native` mode and Eureka running.
- User IDs used are illustrative (id 1 and 2).

1) User signs up (create auth user)

- Client -> Gateway -> POST `/auth/signup`
  - Body: `{ "username": "alice", "password": "pass" }`

- Gateway -> routes to `authservice` -> `AuthController.signup`
  - `authservice` checks `UserRepository.existsByUsername("alice")`.
  - If not exists: `JdbcUserRepository.save()` inserts a row into `authservice.users` table:
    - INSERT INTO users(username, password) VALUES('alice', '<bcrypt-hash>')
  - Returns HTTP 200 "ok" on success.

DB changes:
- `authservice.users` gets new row with username and hashed password.

2) User logs in and obtains a JWT

- Client -> Gateway -> POST `/auth/login` with same credentials.

- `authservice` validates credentials using `JdbcUserRepository.findByUsername` and passwordEncoder.matches.
  - On success: JwtUtil.generateToken("alice") -> returns a JWT.
  - Response: `{ "token": "<jwt>" }`.

Client stores JWT for subsequent calls.

3) Client creates user accounts (application users) via userservice

- Client -> Gateway -> POST `/users` (body `{ "name":":"Alice","credits":100 }`)

- Gateway -> routes to userservice `UserController.createUser`
  - `UserService.createUser` -> `UserRepository.save()` -> inserts into `userservice.users` table:
    - id auto-assigned, name, credits set
  - Response: 200 OK with saved User JSON (id assigned).

DB changes:
- `userservice.users` now has rows for these application-level users.

4) Client triggers a trade via the gateway

- Client -> Gateway -> POST `/trade` with body `{ "from": 1, "to": 2, "amount": 5 }`.

- Gateway forwards to `tradeservice` `TradeController.trade`.

- `TradeService.trade(dto)` performs:
  a) Call userservice to remove credits from `from` user:
     - Current code does: POST `http://localhost:8082/users/1/removeCredits?amount=5`
     - Recommended: use discovery -> `http://userservice/users/1/removeCredits?amount=5` via load-balanced client.
     - If userservice returns non-2xx -> trade fails (returns 400).
  b) Call userservice to add credits to `to` user:
     - POST `/users/2/addCredits?amount=5`
     - If this fails: attempt compensating call to re-add credits to `from` (rollback) and return failure.
  c) If both succeed: create and save new `Trade` in `tradeservice.trades` table:
     - `repo.save()` -> INSERT INTO trades(from_user_id, to_user_id, amount) -> returns saved Trade with id.

- Response to client: 200 OK with created trade JSON.

DB changes:
- `userservice.users` credits updated for both `from` and `to`.
- `tradeservice.trades` contains a new record documenting the transfer.

Data exchanged between services
- `tradeservice` invokes `userservice` endpoints to modify balances. The only shared data is user ids and amounts (no shared DB). The authoritative balance is stored by `userservice`.
- `authservice` stores authentication credentials and issues JWTs. Other services use the token (or the gateway may validate it) to authenticate requests.
- `configurationserver` provides configuration (ports, credentials, shared secrets) to all services. `apigateway` uses a per-service credential to authenticate to downstream services (AuthHeaderFactory values) — these are configured via config server or local properties.

Sequence summary (compact):
1. Client -> Gateway -> `/auth/login` -> authservice -> returns JWT.
2. Client -> Gateway -> `/users` -> userservice -> creates user (DB insert).
3. Client -> Gateway -> `/trade` -> tradeservice -> userservice (removeCredits) -> userservice (addCredits) -> tradeservice DB insert.

## Data sharing & ownership rules

- Each service owns its own data. Communication is via REST APIs only.
- No cross-service foreign key constraints; references are logical only.
- Recommended: treat other services' data as external and access only via stable API; do not read another service's DB directly.

## Security and headers

- Current repository contains two styles mixed together:
  - JWT-based authentication in `authservice` (login returns JWT; services have JwtFilter). This is recommended.
  - Gateway-global Basic auth flow (gateway expects Basic from clients and will call a not-yet-implemented `auth/authenticate`) and then injects service Basic credentials and a shared secret header. This pattern requires consistent trust boundaries and careful secret management.

- Recommendation: standardize on JWTs for clients + either (a) let downstream services validate tokens or (b) make the gateway validate tokens centrally and forward a trusted header to backend services.

---

If you'd like, I can now:
- Update the README with a diagram image or PlantUML file showing the sequence (I can add a .puml file).
- Implement the recommended discovery changes (make `tradeservice` use `http://userservice` via `@LoadBalanced` RestTemplate).
- Implement `/auth/authenticate` in `authservice` to keep the gateway's current filter working (quick change).

Tell me which of those you'd like me to do next and I'll proceed.

# Carbon Credit Trading Microservices

This repository is a skeleton multi-module Spring Boot project for a carbon credit trading platform, modeled after the example microservices layout in the provided classroom project.

Modules:
- servicediscovery (Eureka) - port 8761
- configurationserver (Config Server) - port 8888
- authservice - port 8081
- userservice - port 8082
- carbonservice - port 8083
- tradeservice - port 8084
- apigateway - port 8080

This skeleton contains minimal Application classes and simple REST endpoints for basic flow:
- create users
- inspect and modify user carbon credit balances
- trade credits between users (calls UserService)

How to run (basic): start services in this order for local testing:
1. servicediscovery
2. configurationserver
3. authservice, userservice, carbonservice
4. tradeservice
5. apigateway

Each module is a simple Spring Boot application; the poms use spring-boot-starter-parent and spring-boot-starter-web. This is a starting point — extend with security, persistence, discovery and gateway routing as needed.
