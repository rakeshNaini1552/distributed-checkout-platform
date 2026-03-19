# Eureka Server

## 📌 Overview
Eureka Server acts as the **Service Discovery Registry**.  
All microservices (User Service, Order Service, API Gateway) register themselves here.

---

## 🛠 Tech Stack
- Spring Boot
- Spring Cloud Netflix Eureka

---

## 📂 Project Structure
```
eureka-service/
 ├── src/main/java/com/example/eureka/
 │    └── EurekaServerApplication.java   # Main class with @EnableEurekaServer
 ├── src/main/resources/application.yml
 └── pom.xml
```

---

## ⚙️ Configuration

### application.yml
```yaml
server:
  port: 8761

spring:
  application:
    name: EUREKA-SERVER

eureka:
  client:
    register-with-eureka: false
    fetch-registry: false
```

---

## 🚀 How to Run
1. Run `EurekaServerApplication`
2. Access UI → [http://localhost:8761](http://localhost:8761)
3. See registered services (`USER-SERVICE`, `ORDER-SERVICE`, `API-GATEWAY`)

---

## ✅ How It Fits
- Central registry for microservices
- Gateway & Feign resolve `lb://SERVICE-NAME` dynamically using Eureka
