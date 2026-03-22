# Distributed Checkout Platform

An AI-powered, event-driven microservices platform built with Spring Boot, FastAPI, React, and Kafka. Features a choreography-based saga pattern, LangChain-powered AI recommendations, and a full React frontend — all containerized with Docker Compose.

---

## Architecture Overview

```
                         ┌─────────────────┐
                         │  React Frontend  │
                         │   :3000 (Nginx)  │
                         └────────┬────────┘
                                  │ HTTP
                         ┌────────▼────────┐
                         │   API Gateway   │
                         │     :8080       │
                         │ JWT filter      │
                         │ CORS + routing  │
                         └──┬──────────┬──┘
                            │          │
              ┌─────────────▼──┐  ┌───▼──────────────┐
              │  User Service  │  │  Order Service    │
              │    :8081       │  │    :8082          │
              │ Auth · JWT     │  │ Orders · Feign    │
              │ Register · CRUD│  │ Kafka pub · AI    │
              └───────┬────────┘  └──┬────────────┬──┘
                      │              │            │
              ┌───────▼──────┐       │    ┌───────▼──────────────┐
              │  PostgreSQL  │       │    │  AI Service (FastAPI) │
              │ user_service │       │    │    :8085              │
              └──────────────┘       │    │ Recommend · Triage   │
                                     │    │ NL Search (LangChain)│
              ┌──────────────┐       │    └──────────────────────┘
              │  PostgreSQL  │       │
              │ order_service│◄──────┘
              └──────────────┘       │
                                     │ Kafka: order-events
                              ┌──────▼───────┐
                              │    Kafka      │
                              │    :9092      │
                              └──┬───────────┘
                                 │
                    ┌────────────▼────────────┐
                    │   Inventory Service      │
                    │      :8083               │
                    │ Stock check · Saga       │
                    └──────────┬──────────────┘
                               │
                    ┌──────────▼──────────┐
                    │     PostgreSQL       │
                    │  inventory_service   │
                    └─────────────────────┘

              ┌──────────────────────────────┐
              │    Product Service :8084      │
              │  Catalog · Seed data · REST  │
              └──────────────────────────────┘

              ┌──────────────────────────────┐
              │    Eureka Server :8761        │
              │    Service Registry           │
              └──────────────────────────────┘
```

---

## Services

| Service | Port | Tech | Responsibility |
|---|---|---|---|
| frontend | 3000 | React + Nginx | UI — register, login, products, orders, recommendations |
| gateway-service | 8080 | Spring Cloud Gateway | Single entry point, JWT validation, CORS, routing |
| user-service | 8081 | Spring Boot | Auth, JWT generation, user CRUD |
| order-service | 8082 | Spring Boot | Orders, Feign, Kafka producer, AI integration |
| inventory-service | 8083 | Spring Boot | Stock check, saga consumer/producer |
| product-service | 8084 | Spring Boot | Product catalog, seed data |
| ai-service | 8085 | FastAPI + LangChain | Recommendations, incident triage, NL search |
| eureka-service | 8761 | Spring Cloud Eureka | Service registry |

---

## System Design

### Design Patterns Used

**1. API Gateway Pattern**
All client requests go through a single entry point. The Gateway handles JWT validation, CORS, and routes requests to downstream services via Eureka service discovery (`lb://SERVICE-NAME`).

**2. Choreography-based Saga Pattern**
Order fulfillment is coordinated through Kafka events — no central orchestrator. Each service reacts to events and publishes results.

```
Order Service  →  [order-events]             →  Inventory Service
                                                       ↓ stock check
Inventory      →  [order-confirmed-events]   →  Order Service → status: CONFIRMED
Inventory      →  [order-rejected-events]    →  Order Service → status: REJECTED
```

**3. Circuit Breaker + Retry (Resilience4j)**
Feign calls to User Service and Product Service are wrapped with circuit breakers. If a service is down, fallback responses are returned and the system degrades gracefully.

**4. Async AI Integration**
AI recommendations are generated asynchronously after order creation using `@Async` + WebClient. The main order flow is never blocked by AI latency.

**5. Service Discovery (Eureka)**
All Spring Boot services register with Eureka. The Gateway and Feign clients resolve service addresses dynamically — no hardcoded URLs between services.

---

## Database Schemas

### `user_service`

```sql
CREATE TABLE users (
    id          SERIAL PRIMARY KEY,
    first_name  VARCHAR NOT NULL,
    last_name   VARCHAR NOT NULL,
    email       VARCHAR UNIQUE NOT NULL,
    password    VARCHAR NOT NULL,          -- BCrypt hashed
    role        VARCHAR NOT NULL           -- USER, ADMIN, SUPER_ADMIN
);
```

### `order_service`

```sql
CREATE TABLE orders (
    id          SERIAL PRIMARY KEY,
    user_id     INTEGER NOT NULL,          -- logical ref to users.id
    item        VARCHAR NOT NULL,          -- denormalized product name
    price       DECIMAL NOT NULL,          -- denormalized product price
    status      VARCHAR NOT NULL,          -- CREATED, CONFIRMED, REJECTED, COMPLETED, CANCELLED
    created_at  TIMESTAMP
);

CREATE TABLE order_recommendations (
    id           SERIAL PRIMARY KEY,
    order_id     INTEGER NOT NULL,         -- logical ref to orders.id
    product_name VARCHAR NOT NULL,
    created_at   TIMESTAMP
);
```

### `product_service`

```sql
CREATE TABLE products (
    id          SERIAL PRIMARY KEY,
    name        VARCHAR UNIQUE NOT NULL,
    description VARCHAR,
    price       DECIMAL NOT NULL,
    category    VARCHAR,
    image_url   VARCHAR
);
```

### `inventory_service`

```sql
CREATE TABLE inventory (
    id          SERIAL PRIMARY KEY,
    item_name   VARCHAR UNIQUE NOT NULL,   -- matches products.name
    quantity    INTEGER NOT NULL,
    updated_at  TIMESTAMP
);
```

> **Note:** No foreign keys exist across service databases. Referential integrity is maintained at the application level through Feign calls and Kafka events. This is intentional microservices design — each service owns its data independently.

---

## Sequence Diagrams

### 1. User Registration & Login

```
React          Gateway        User Service    PostgreSQL
  │                │                │               │
  │─POST /users/user──────────────►│               │
  │                │────────────────►               │
  │                │                │──INSERT users─►│
  │                │                │◄──────────────│
  │◄──201 Created──────────────────│               │
  │                │                │               │
  │─POST /auth/login──────────────►│               │
  │                │────────────────►               │
  │                │                │──SELECT users─►│
  │                │                │◄──────────────│
  │                │                │ BCrypt verify  │
  │◄──{ jwt: "..." }───────────────│               │
```

### 2. Place Order — Full Saga Flow

```
React    Gateway  Order Svc  Product Svc  Kafka     Inventory Svc  AI Svc
  │         │        │           │          │             │           │
  │─POST────►│        │           │          │             │           │
  │  /orders │        │           │          │             │           │
  │  /create │────────►           │          │             │           │
  │          │        │─Feign─────►          │             │           │
  │          │        │◄──product details────│             │           │
  │          │        │─save order(CREATED)  │             │           │
  │          │        │──publish OrderCreatedEvent─────────►           │
  │◄─200 OK──│        │                      │             │           │
  │  CREATED │        │──@Async WebClient────│─────────────────────────►
  │          │        │                      │  stock check│           │
  │          │        │                      │  decrement  │           │
  │          │        │                      │◄─OrderConfirmedEvent────│
  │          │        │◄─────────────────────│             │           │
  │          │        │ update status        │             │           │
  │          │        │ CONFIRMED            │             │           │
  │          │        │                      │             │           │
  │          │        │◄──────────────────────────────────│  rec[]    │
  │          │        │ save recommendations │             │           │
```

### 3. Get Order Recommendations

```
React          Gateway        Order Service   order_recommendations
  │                │                │               │
  │─GET /orders/{id}/recommendations──────────────►│
  │                │────────────────►               │
  │                │                │──SELECT WHERE order_id=?──►│
  │                │                │◄──────────────────────────│
  │◄──[{productName}]──────────────│               │
```

### 4. Recommendation Modal — Product Lookup

```
React          Gateway        Product Service
  │                │                │
  │─GET /products/name/{name}──────►│
  │                │────────────────►
  │                │                │ SELECT WHERE name=?
  │◄──{ id, name, price, imageUrl }─│
  │  show modal    │                │
  │─POST /orders/create─────────────►  (if user clicks Place Order)
```

---

## Kafka Topics

| Topic | Producer | Consumer | Event |
|---|---|---|---|
| `order-events` | Order Service | Inventory Service | New order created |
| `order-confirmed-events` | Inventory Service | Order Service | Stock available, order confirmed |
| `order-rejected-events` | Inventory Service | Order Service | Out of stock or item not found |

---

## AI Service Endpoints

| Endpoint | Input | Output | When Called |
|---|---|---|---|
| `POST /ai/recommend` | orderId, purchasedItem, category, availableProducts | List of 3 product names | Async after order creation |
| `POST /ai/triage` | List of recent failed orders | summary, severity (1-5), recommendedAction | When circuit breaker opens |
| `POST /ai/search` | userId, natural language query | Structured filters (item, status, dateFrom, dateTo) | NL search feature |

---

## Prerequisites

- Docker Desktop
- Java 17
- Maven
- Python 3.9+
- Node.js 18+

---

## Environment Variables

Create a `.env` file or export these in your shell:

```bash
JWT_SECRET=your_jwt_secret_min_32_chars
DB_PASSWORD=your_postgres_password
GROQ_API_KEY=your_groq_api_key
```

---

## Quick Start

```bash
# Clone the repo
git clone https://github.com/rakeshNaini1552/distributed-checkout-platform.git
cd distributed-checkout-platform

# Set environment variables
export JWT_SECRET=this_is_a_super_long_secret_key_with_32+_chars!
export DB_PASSWORD=0000
export GROQ_API_KEY=your_groq_api_key

# Build all Spring Boot jars
cd Common-events-kafka && mvn clean install -DskipTests && cd ..
cd user-service && mvn clean package -DskipTests && cd ..
cd order-service && mvn clean package -DskipTests && cd ..
cd product-service && mvn clean package -DskipTests && cd ..
cd inventory-service && mvn clean package -DskipTests && cd ..
cd GATEWAY_SERVICE && mvn clean package -DskipTests && cd ..
cd eureka-service && mvn clean package -DskipTests && cd ..

# Start everything
docker compose up --build
```

Open `http://localhost:3000`

---

## Startup Sequence (Manual)

If running services locally without Docker:

```
1. PostgreSQL        → brew services start postgresql@15
2. Kafka + Zookeeper → brew services start kafka
3. common-events     → mvn clean install -DskipTests
4. Eureka            → mvn spring-boot:run  (:8761)
5. User Service      → mvn spring-boot:run  (:8081)
6. Product Service   → mvn spring-boot:run  (:8084)
7. Inventory Service → mvn spring-boot:run  (:8083)
8. Order Service     → mvn spring-boot:run  (:8082)
9. Gateway           → mvn spring-boot:run  (:8080)
10. AI Service       → uvicorn main:app --port 8085
11. Frontend         → npm start             (:3000)
```

---

## API Reference

### Auth
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/auth/login` | None | Login, returns JWT |
| POST | `/users/user` | None | Register new user |

### Products
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/products` | JWT | Get all products |
| GET | `/products/{id}` | JWT | Get product by ID |
| GET | `/products/name/{name}` | JWT | Get product by name |
| GET | `/products/category/{category}` | JWT | Get by category |

### Orders
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| POST | `/orders/create` | JWT | Place an order |
| GET | `/orders/{id}` | JWT | Get order details |
| GET | `/orders/user/{userId}` | JWT | Get all user orders |
| GET | `/orders/{id}/recommendations` | JWT | Get AI recommendations |

### Inventory
| Method | Endpoint | Auth | Description |
|---|---|---|---|
| GET | `/inventory` | None | Get all inventory |
| GET | `/inventory/{id}` | None | Get by ID |
| GET | `/inventory/item/{name}` | None | Get by item name |
| POST | `/inventory` | None | Add inventory item |
| PUT | `/inventory/{id}?quantity=N` | None | Update stock quantity |

---

## Testing the Saga

**Confirm flow (Laptop — in stock):**
```json
POST /orders/create
{ "productId": 1 }
```
Wait 2s → `GET /orders/{id}` → status: `CONFIRMED`

**Reject flow (SSD Drive — out of stock):**
```json
POST /orders/create
{ "productId": 10 }
```
Wait 2s → `GET /orders/{id}` → status: `REJECTED`

---

## Known Limitations & Future Work

**Outbox Pattern** — `createOrder` saves to DB and publishes to Kafka in sequence. If Kafka publish fails after DB commit, the order stays `CREATED` permanently. Production fix: implement the Outbox Pattern for guaranteed event delivery.

**JWT Secret rotation** — currently a single static secret. Production: use asymmetric keys (RS256) with key rotation.

**AI Service not in Eureka** — the FastAPI AI service doesn't register with Eureka. Order Service calls it via hardcoded URL from config. Production: add a service mesh or register via a sidecar.

**No pagination** — `GET /orders/user/{userId}` returns all orders. Production: add `Pageable` support.

**Inventory not linked to product catalog** — inventory items are matched by name string. Production: add `productId` FK to inventory table.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Frontend | React 19, React Router, Axios, Nginx |
| API Gateway | Spring Cloud Gateway, JJWT |
| Backend Services | Spring Boot 4.x, Spring Data JPA, Spring Security |
| AI Service | FastAPI, LangChain, Groq (Llama 3.3) |
| Messaging | Apache Kafka |
| Service Discovery | Netflix Eureka |
| Resilience | Resilience4j (Circuit Breaker, Retry) |
| Databases | PostgreSQL 15 |
| Containerization | Docker, Docker Compose |
| Build | Maven, npm |

---

## Project Structure

```
distributed-checkout-platform/
├── Common-events-kafka/       # Shared event classes
│   └── src/main/java/com/example/events/
│       ├── OrderCreatedEvent.java
│       ├── OrderConfirmedEvent.java
│       ├── OrderRejectedEvent.java
│       └── OrderStatus.java
├── eureka-service/            # Service registry :8761
├── GATEWAY_SERVICE/           # API Gateway :8080
├── user-service/              # Auth + users :8081
├── order-service/             # Orders + AI :8082
├── inventory-service/         # Stock management :8083
├── product-service/           # Product catalog :8084
├── ai-service/                # FastAPI AI :8085
│   ├── main.py
│   ├── app/
│   │   ├── routes.py
│   │   ├── services.py
│   │   └── models/
│   └── requirements.txt
├── frontend/                  # React UI :3000
│   ├── src/
│   │   ├── pages/
│   │   ├── components/
│   │   ├── api/
│   │   └── App.css
│   ├── Dockerfile
│   └── nginx.conf
├── docker-compose.yml
├── init-db.sql
└── README.md
```