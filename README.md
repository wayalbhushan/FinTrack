# FinTrack API

[![Java 17](https://img.shields.io/badge/Java-17%2B-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)](https://www.oracle.com/java/)
[![Spring Boot 3](https://img.shields.io/badge/Spring%20Boot-3.x-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)](https://spring.io/projects/spring-boot)
[![Spring Security](https://img.shields.io/badge/Spring%20Security-6.x-6DB33F?style=for-the-badge&logo=springsecurity&logoColor=white)](https://spring.io/projects/spring-security)
[![PostgreSQL](https://img.shields.io/badge/PostgreSQL-15-316192?style=for-the-badge&logo=postgresql&logoColor=white)](https://www.postgresql.org/)
[![Docker](https://img.shields.io/badge/Docker-Enabled-2496ED?style=for-the-badge&logo=docker&logoColor=white)](https://www.docker.com/)
[![Render](https://img.shields.io/badge/Render-Deployed-46E3B7?style=for-the-badge&logo=render&logoColor=white)](https://render.com/)

A secure, high-performance Personal Finance Management REST API designed to track income, expenses, custom savings goals with dynamic progress tracking, and generate monthly/yearly financial analytics. Built using Spring Boot 3, Spring Security, and PostgreSQL, the project is structured with production-grade architectural patterns, strict tenant isolation, and automated containerized deployments.

---

## 🛠️ Architectural Highlights

*   **Multi-Tenant Data Isolation**: Implements strict data isolation. Data ownership is contextually bound to the authenticated principal in Spring Security. Users can only query, update, or delete data belonging to their own user context, completely preventing IDOR (Insecure Direct Object Reference) vulnerabilities.
*   **Dynamic Query Optimization via JPA Criteria API**: Replaces dynamic JPQL null-check patterns with database-agnostic JPA Criteria API queries. This eliminates database driver level parameter type-inference bugs in PostgreSQL and compiles query branches at runtime with optimal index traversal.
*   **Secure Session-Based JWT Token Blacklisting**: Utilizes JWT-based session cookies (`SESSION_TOKEN`) marked as `HttpOnly`, `Secure`, and `SameSite=Strict`. Upon logout, the token is added to an in-memory token blacklist to invalidate requests and defend against token replay attacks.
*   **Schema Evolution & Migration Handling**: Features a dynamic custom startup datasource initializer that automatically resolves PostgreSQL schema mismatches. If it detects a legacy UUID column configuration, it drops dependent constraints and regenerates the database schema to conform to high-performance identity bigint keys automatically.
*   **RFC 7807 Exception Mapping**: Implements global Exception Handling (`@ControllerAdvice`) mapping constraints and system exceptions to clean, RFC-compliant Problem Details JSON representations. This guarantees zero `5xx` errors for known client error conditions and keeps stack traces hidden from the API layer.

---

## 🚀 Quick Start (Local Setup)

### Prerequisites
*   Java Development Kit (JDK) 17 or higher
*   Apache Maven 3.9+
*   PostgreSQL (Optional, only for running production profile locally)

### 1. Clone the Repository
```bash
git clone https://github.com/wayalbhushan/FinTrack.git
cd FinTrack
```

### 2. Configure Environment Profiles
By default, the application runs under the `local` profile which uses an in-memory **H2 Database** and does not require PostgreSQL.

For production, settings are configured under the `prod` profile using environment variables:
```bash
# Required for running production profile
DATABASE_URL=postgres://<username>:<password>@<host>:<port>/<db_name>
JWT_SECRET=your_super_secret_base64_or_hex_signing_key_here
```

### 3. Build the Application
```bash
mvn clean package -DskipTests
```

### 4. Run the Server
*   **Local Profile (H2 In-Memory DB)**:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=local
    ```
*   **Production Profile (PostgreSQL)**:
    ```bash
    mvn spring-boot:run -Dspring-boot.run.profiles=prod
    ```

The API will bind and listen at `http://localhost:8080/api`.

---

## 📑 API Specifications & Contracts

All requests and responses communicate via JSON. Most endpoints require authentication via the `SESSION_TOKEN` cookie.

### 1. Authentication Endpoints

| Endpoint | Method | Authentication | Request Body (JSON) | Description |
| :--- | :--- | :--- | :--- | :--- |
| `/api/auth/register` | `POST` | None | `{"username": "user@example.com", "password": "securePass", "fullName": "John Doe", "phoneNumber": "+123456"}` | Register new user. |
| `/api/auth/login` | `POST` | None | `{"username": "user@example.com", "password": "securePass"}` | Returns `SESSION_TOKEN` Cookie. |
| `/api/auth/logout` | `POST` | Required | None | Invalidates the cookie and blacklists JWT. |

### 2. Transactions

| Endpoint | Method | Request Body (JSON) / Params | Description |
| :--- | :--- | :--- | :--- |
| `/api/transactions` | `POST` | `{"amount": 50.00, "date": "2026-05-24", "category": "Food", "description": "Lunch"}` | Create a transaction. |
| `/api/transactions` | `GET` | *Optional Params*: `?startDate=YYYY-MM-DD&endDate=YYYY-MM-DD&category=Food` | Retrieve filtered transactions. |
| `/api/transactions/{id}` | `PUT` | `{"amount": 60.00, "description": "Updated Lunch"}` *(Date updates ignored)* | Update transaction. |
| `/api/transactions/{id}` | `DELETE` | None | Delete transaction. |

### 3. Categories

| Endpoint | Method | Request Body (JSON) / Params | Description |
| :--- | :--- | :--- | :--- |
| `/api/categories` | `GET` | None | Retrieve default and custom categories. |
| `/api/categories` | `POST` | `{"name": "Freelance", "type": "INCOME"}` | Create custom category. |
| `/api/categories/{name}` | `DELETE` | None | Delete custom category (if not in use). |

### 4. Savings Goals

| Endpoint | Method | Request Body (JSON) / Params | Description |
| :--- | :--- | :--- | :--- |
| `/api/goals` | `POST` | `{"goalName": "Home", "targetAmount": 1000.0, "targetDate": "2028-12-31"}` | Create a goal. |
| `/api/goals` | `GET` | None | Retrieve goals with progress percentages. |
| `/api/goals/{id}` | `GET` | None | Retrieve single goal detail. |
| `/api/goals/{id}` | `PUT` | `{"targetAmount": 1200.0}` | Update goal. |
| `/api/goals/{id}` | `DELETE` | None | Delete goal. |

### 5. Analytics & Reports

| Endpoint | Method | Description |
| :--- | :--- | :--- |
| `/api/reports/monthly/{year}/{month}` | `GET` | Aggregated Category sum & Net savings for the month. |
| `/api/reports/yearly/{year}` | `GET` | Aggregated Category sum & Net savings for the year. |

---

## 🧪 Testing & Deployment

### Local Unit Testing
Run the comprehensive suite of unit and integration tests:
```bash
mvn test
```

### End-to-End Verification
To test the API logic dynamically against the test harness:
```bash
# Starts local server and executes shell test suite
./financial_manager_tests.sh http://localhost:8080/api
```

### Production Deployment
The API is deployed on **Render.com** and connected to Render PostgreSQL.
*   **Infrastructure-as-Code Configuration**: [render.yaml](file:///c:/Users/wayal/Desktop/FinTechApp/render.yaml)
*   **Production Deployment URL**: `https://fintrack-api-8yt3.onrender.com/api`
*   **Live E2E Verification**:
    ```bash
    ./financial_manager_tests.sh https://fintrack-api-8yt3.onrender.com/api
    ```
