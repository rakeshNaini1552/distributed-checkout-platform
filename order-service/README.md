# Order Service

## 📌 Overview
Order Service is responsible for:
- Managing Orders (CRUD)
- Fetching User details from **User Service** via Feign Client
- Using **Resilience4j Retry & CircuitBreaker** with fallback
- Registers with **Eureka Server**
- Routed via **API Gateway**

---

## 🛠 Tech Stack
- Spring Boot
- Spring Data JPA
- Spring Cloud OpenFeign
- Resilience4j
- Eureka Client

---

## 📂 Project Structure
```
order-service/
 ├── src/main/java/com/example/order/
 │    ├── controller/OrderController.java      # Order APIs
 │    ├── service/OrderService.java            # Business logic
 │    ├── client/UserClient.java               # Feign client -> USER-SERVICE
 │    └── OrderServiceApplication.java         # Main class (@EnableEurekaClient)
 ├── src/main/resources/application.yml
 └── pom.xml
```

---

## ⚙️ Configuration

### application.yml
```yaml
server:
  port: 8082

spring:
  application:
    name: ORDER-SERVICE

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka

resilience4j:
  retry:
    instances:
      userServiceRetry:
        max-attempts: 3
        wait-duration: 2s
  circuitbreaker:
    instances:
      userServiceCB:
        sliding-window-size: 10
        failure-rate-threshold: 50
        wait-duration-in-open-state: 10s

management:
  endpoints:
    web:
      exposure:
        include: circuitbreakers,circuitbreakerevents,health,info
```

---

## 🚀 How to Run
1. Start **Eureka Server** & **User Service**
2. Run `OrderServiceApplication`
3. Service registers as `ORDER-SERVICE`

---

## 🔑 APIs
- `POST /orders` → Create a new order  
- `GET /orders/{id}` → Get order details  
- `GET /orders/user/{userId}` → Fetch orders + User details  

---

## 🔗 Dependencies
- Requires **Eureka Server** for discovery
- Requires **User Service** for Feign calls
- Routed via **API Gateway** (lb://ORDER-SERVICE)

---

## ✅ Resilience4j Flow
- Feign calls User Service
- If failure → retries 3 times
- If still fails → CircuitBreaker opens → fallback returns placeholder user
