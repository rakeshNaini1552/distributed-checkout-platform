# User Service

## 📌 Overview
User Service is responsible for:
- **User registration and authentication**
- **JWT token generation & validation**
- Provides secured APIs for user details
- Registers itself with **Eureka Server**
- Can be routed via **API Gateway**

---

## 🛠 Tech Stack
- Spring Boot
- Spring Security + JWT
- Spring Data JPA (optional database)
- Eureka Client

---

## 📂 Project Structure
```
user-service/
 ├── src/main/java/com/example/user/
 │    ├── controller/AuthController.java      # Login & Signup APIs
 │    ├── controller/UserController.java      # User-related APIs
 │    ├── security/JwtUtil.java               # JWT utilities
 │    ├── security/JwtRequestFilter.java      # Token validation filter
 │    ├── config/SecurityConfig.java          # Spring Security setup
 │    └── UserServiceApplication.java         # Main class (@EnableEurekaClient)
 ├── src/main/resources/application.yml
 └── pom.xml
```

---

## ⚙️ Configuration

### application.yml
```yaml
server:
  port: 8081

spring:
  application:
    name: USER-SERVICE

eureka:
  client:
    service-url:
      defaultZone: http://localhost:8761/eureka
```

---

## 🚀 How to Run
1. Ensure **Eureka Server** is running at [http://localhost:8761](http://localhost:8761)
2. Run `UserServiceApplication`
3. Access service → [http://localhost:8081](http://localhost:8081)

---

## 🔑 APIs
- `POST /auth/register` → Create new user  (ToDo)
- `POST /auth/login` → Authenticate & receive JWT  
- `GET /users/{id}` → Get user details *(JWT required)*  

For secured APIs, include:  
```
Authorization: Bearer <JWT_TOKEN>
```

---

## 🔗 Dependencies
- Requires **Eureka Server** for service discovery
- Routed via **API Gateway** (lb://USER-SERVICE)

---

## ✅ How It Fits
- Provides authentication for **Order Service**
- JWT validated by `JwtRequestFilter` before controller execution
