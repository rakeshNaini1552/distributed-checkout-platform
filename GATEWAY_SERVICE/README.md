# API Gateway

## 📌 Overview
API Gateway is the **single entry point** for all clients.  
It routes requests to the correct microservice using Eureka service discovery.

---

## 🛠 Tech Stack
- Spring Boot
- Spring Cloud Gateway
- Eureka Client

---

## 📂 Project Structure
```
gateway-service/
 ├── src/main/java/com/example/gateway/
 │    └── GatewayServiceApplication.java   # Main class
 ├── src/main/resources/application.yml
 └── pom.xml
```

---

## ⚙️ Configuration

### application.yml
```yaml
server:
  port: 8080

spring:
  application:
    name: API-GATEWAY
  cloud:
    gateway:
      discovery.locator.enabled: true
      routes:
        - id: user-service
          uri: lb://USER-SERVICE
          predicates:
            - Path=/auth/**,/users/**
        - id: order-service
          uri: lb://ORDER-SERVICE
          predicates:
            - Path=/orders/**

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## 🚀 How to Run
1. Start **Eureka Server**
2. Start **User Service** & **Order Service**
3. Run `GatewayServiceApplication`
4. Gateway auto-discovers services

---

## 🔑 Routes
- `/auth/**` → User Service
- `/users/**` → User Service
- `/orders/**` → Order Service

---

## ✅ How It Fits
- Client → Gateway → Microservice
- Uses `lb://SERVICE-NAME` to dynamically route requests via Eureka
