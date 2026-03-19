# Spring Microservices Project

A microservices system built with Spring Boot covering user authentication, order management, event-driven communication via Kafka, and service discovery via Eureka.

---

## Architecture overview

```
Client (Postman / UI)
        |
        v
API Gateway :8080          ← JWT auth filter, routes all requests
        |
   _____|_____
  |           |
  v           v
User Service  Order Service
  :8081         :8082
  |               |
  v               v
PostgreSQL    PostgreSQL      ← separate DBs: user_service, order_service
(user_service) (order_service)
        |           |
        |___________| 
              |
              v
           Kafka :9092        ← Order Service publishes, User Service consumes
              |
        topic: order-events

All services register with:
Eureka Server :8761
```

### Services

| Service | Port | Responsibility |
|---|---|---|
| eureka-server | 8761 | Service registry — all services register here |
| gateway-service | 8080 | Single entry point, JWT validation, routing |
| user-service | 8081 | Auth, JWT generation, user CRUD |
| order-service | 8082 | Order CRUD, Kafka producer, Feign client to user-service |
| common-events | — | Shared library (OrderCreatedEvent, OrderStatus) |

---

## Prerequisites

Make sure the following are installed before starting:

- Java 17 (Zulu or any distribution)
- Maven (or use the `./mvnw` wrapper in each service)
- PostgreSQL 15
- Kafka (via Homebrew or Docker)
- pgAdmin 4 (optional, for DB inspection)

### macOS setup (Homebrew)

```bash
# Install Kafka
brew install kafka

# Set Java 17 as default (if not already)
echo 'export JAVA_HOME=/Library/Java/JavaVirtualMachines/zulu-17.jdk/Contents/Home' >> ~/.zshrc
echo 'export PATH="$JAVA_HOME/bin:$PATH"' >> ~/.zshrc
source ~/.zshrc
```

---

## Database setup

Start PostgreSQL:

```bash
brew services start postgresql@15
```

Connect and create the two databases:

```bash
psql postgres -U <your-mac-username>
```

```sql
CREATE USER postgres WITH SUPERUSER PASSWORD '0000';
CREATE DATABASE user_service OWNER postgres;
CREATE DATABASE order_service OWNER postgres;
\q
```

> Hibernate will auto-create all tables (`users`, `orders`) on first service boot via `ddl-auto: update`.

---

## Startup sequence

> Order matters. Always start infrastructure first, then services bottom-up.

### Step 1 — Start Kafka

```bash
brew services start kafka
```

Verify:

```bash
kafka-topics --list --bootstrap-server localhost:9092
# should return empty (no topics yet)
```

### Step 2 — Install the shared library

```bash
cd common-events
./mvnw install
```

This installs `common-events-1.0.jar` into your local `~/.m2` so Order Service and User Service can resolve it at build time.

### Step 3 — Start Eureka Server

```bash
cd eureka-server
./mvnw spring-boot:run
```

Wait for `Started EurekaServerApplication`, then open [http://localhost:8761](http://localhost:8761).

### Step 4 — Start User Service

```bash
cd user-service
./mvnw spring-boot:run
```

Wait for `Started UserServiceApplication`. Check [http://localhost:8761](http://localhost:8761) — `USER-SERVICE` should appear.

### Step 5 — Start Order Service

```bash
cd order-service
./mvnw spring-boot:run
```

Wait for `Started OrderApplication`. `ORDER-SERVICE` should appear in Eureka.

### Step 6 — Start API Gateway

```bash
cd gateway-service
./mvnw spring-boot:run
```

Wait for `Started GatewayApplication`. `GATEWAY-SERVICE` should appear in Eureka.

All four services should now be listed as `UP` at [http://localhost:8761](http://localhost:8761).

---

## First-time user setup

The `users` table will be empty on first run. Insert a user directly via pgAdmin or psql with a BCrypt-hashed password.

To generate the correct BCrypt hash for your password, temporarily add this line to `UserServiceApplication.java` main method:

```java
System.out.println(new BCryptPasswordEncoder().encode("yourpassword"));
```

Run User Service, copy the printed hash, then insert via pgAdmin Query Tool:

```sql
INSERT INTO public.users (first_name, last_name, email, password, role)
VALUES ('First', 'Last', 'user@email.com', '<bcrypt-hash>', 'USER');
```

Remove the `System.out.println` line after.

---

## Testing with Postman

### 1. Login

```
POST http://localhost:8080/auth/login
Content-Type: application/json

{
  "username": "user@email.com",
  "password": "yourpassword"
}
```

Response: `{ "jwt": "<token>" }`

### 2. Create an order

```
POST http://localhost:8080/orders/create
Authorization: Bearer <token>
Content-Type: application/json

{
  "item": "Laptop",
  "price": 999.99
}
```

Response:

```json
{
  "id": 1,
  "userId": 1,
  "item": "Laptop",
  "price": 999.99,
  "status": "CREATED",
  "createdAt": "2026-03-19T16:29:58.833103"
}
```

After this, check the User Service terminal — you should see the Kafka event consumed:

```
Received OrderCreatedEvent in UserService:
  Order ID: 1
  User ID: 1
  Item: Laptop
  Price: 999.99
```

### 3. Get order details (includes user info via Feign)

```
GET http://localhost:8080/orders/{id}
Authorization: Bearer <token>
```

### 4. Get all orders for a user

```
GET http://localhost:8080/orders/user/{userId}
Authorization: Bearer <token>
```

---

## Request flow through the Gateway

Every request hits the `JwtAuthenticationFilter` (GlobalFilter) first:

1. `/auth/login` → bypassed, no token needed
2. All other paths → must have `Authorization: Bearer <token>`
3. Token is validated with JJWT using HS256
4. If valid → `X-User-Id` header is injected and request is routed via Eureka (`lb://SERVICE-NAME`)
5. If invalid or missing → `401 Unauthorized`

---

## Shutdown sequence

Shut down in reverse order — Gateway first, Eureka last. Press `Ctrl+C` in each terminal:

1. Gateway terminal → `Ctrl+C`
2. Order Service terminal → `Ctrl+C`
3. User Service terminal → `Ctrl+C`
4. Eureka Server terminal → `Ctrl+C`

Then stop infrastructure:

```bash
brew services stop kafka
brew services stop postgresql@15
```

Verify:

```bash
brew services list | grep -E "kafka|postgresql"
# both should show: stopped
```

---

## Known issues and notes

**Secret key is hardcoded** — the JWT secret `"this_is_a_super_long_secret_key_with_32+_chars!"` is hardcoded in three places: `JwtAuthenticationFilter` (Gateway), `JwtUtil` (Order Service), and `JwtUtil` (User Service). The `jwt.secret` property in `application.yml` is not wired up and has no effect. For production, externalize this to Spring Cloud Config or AWS Secrets Manager.

**No user registration endpoint via Gateway** — `POST /users/user` is protected at both the Gateway and User Service `SecurityConfig` levels. New users must be inserted directly into the database (see First-time user setup above), or the endpoint needs to be whitelisted.

**Duplicate OrderCreatedEvent class** — Order Service defines its own `com.example.order.events.OrderCreatedEvent` locally while User Service uses the one from `common-events`. Both work due to `TRUSTED_PACKAGES=*` in Kafka config, but ideally Order Service should also use the shared module's class directly.

**Feign JWT propagation** — `FeignConfig` reads the incoming `Authorization` header from `RequestContextHolder` and forwards it to User Service. This works for synchronous REST calls but will not work in async contexts (e.g. inside a new thread or a `@Async` method).
