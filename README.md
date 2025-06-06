# Online Bus Ticket Booking System (Microservices Architecture)

This is an online bus ticket booking system built with a **Microservices Architecture** using **Spring Boot**, **Spring Cloud**, **Kafka**, and other modern tools. It allows users to register, search for buses, book tickets, make payments, receive notifications, and chat with bus operators.

---

## 🚀 Tech Stack

- **Java 21**
- **Spring Boot 3.x**
- **Spring Cloud** (Eureka, Gateway, Config)
- **Spring Security + JWT**
- **Spring Data JPA (MySQL)**
- **WebClient** (for inter-service communication)
- **Apache Kafka** (for async communication)
- **Docker + Docker Compose**
- **WebSocket** (for real-time chat)

---

## 🧩 Microservices Overview

| Service Name         | Description                                                                 |
|----------------------|-----------------------------------------------------------------------------|
| `discovery-server`   | Eureka Server for service registry                                          |
| `gateway-service`    | API Gateway for routing and security                                        |
| `auth-service`       | Handles user authentication and JWT token generation                        |
| `bus-service`        | Manages buses, routes, schedules, seat maps                                 |
| `booking-service`    | Handles ticket booking and seat selection                                   |
| `payment-service`    | Manages payment integration (e.g., VNPAY)                                   |
| `notification-service` | Sends email notifications via Kafka                                        |
| `report-service`     | Generates statistics and reporting for admin                                |
| `chat-service`       | WebSocket-based chat between users and bus operators                        |

---

## ⚙️ How to Run (Locally with Docker Compose)

```bash
# Clone the repository
git clone https://github.com/ducbn/Book_ticket_bus_with_SpringBoot_Microservices.git
cd Book_ticket_bus_with_SpringBoot_Microservices

# Start all services with Docker Compose
docker-compose up --build
