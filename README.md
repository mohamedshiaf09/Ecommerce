# E-Commerce System — Spring Boot Microservices

Two independent Spring Boot applications — **Product Service** and **Order Service** —
communicating over REST to simulate a simple e-commerce order flow.

```
ECommerce-System
│
├── product-service   (port 8080)
└── order-service     (port 8081)
```

## 1. Architecture

- **Layered architecture** in both services: `Controller → Service → Repository`
- **DTOs**: requests/responses use DTOs (`ProductDTO`, `OrderRequestDTO`, `OrderResponseDTO`)
  instead of exposing JPA entities directly
- **Inter-service communication**: Order Service calls Product Service via `RestTemplate`
  (bean in `order-service/.../config/RestTemplateConfig.java`), wrapped by
  `order-service/.../client/ProductClient.java`
- **Validation**: `jakarta.validation` annotations (`@NotBlank`, `@NotNull`, `@Min`) on DTOs,
  enforced with `@Valid` in controllers
- **Error handling**: `@RestControllerAdvice` global exception handlers in both services
  return structured JSON errors — 404 (product/order not found), 409 (insufficient stock),
  400 (validation errors)

## 2. Tech Stack

| Layer          | Technology                          |
|----------------|--------------------------------------|
| Language        | Java 17                             |
| Framework       | Spring Boot (Spring Web, Spring Data JPA) |
| Database        | H2 (in-memory)                      |
| Boilerplate     | Lombok                              |
| Build tool      | Maven                               |
| Inter-service   | RestTemplate                        |

## 3. Prerequisites

- JDK 17 (required — Lombok annotation processing in this project is not compatible
  with newer JDKs such as 21/26)
- Maven 3.8+

No external database setup is needed — each service uses an **H2 in-memory database**
that is created automatically on startup and reset every time the service restarts.

## 4. Running the Services

Start **Product Service** first (Order Service depends on it being reachable):

```bash
cd product-service
mvn spring-boot:run
```

Then, in a separate terminal, start **Order Service**:

```bash
cd order-service
mvn spring-boot:run
```

| Service          | Base URL                     | H2 Console                              |
|-------------------|-------------------------------|------------------------------------------|
| Product Service   | http://localhost:8080         | http://localhost:8080/h2-console (JDBC URL: `jdbc:h2:mem:productdb`) |
| Order Service     | http://localhost:8081         | http://localhost:8081/h2-console (JDBC URL: `jdbc:h2:mem:orderdb`)   |

H2 console login: username `sa`, no password.

## 5. API Reference

### Product Service (`localhost:8080`)

| Method | Endpoint                        | Description                          |
|--------|----------------------------------|---------------------------------------|
| POST   | `/products`                      | Add a new product                     |
| GET    | `/products`                      | List all products                     |
| GET    | `/products/{id}`                 | Get a product by ID                   |
| PUT    | `/products/{id}`                 | Update a product                      |
| DELETE | `/products/{id}`                 | Delete a product                      |
| PUT    | `/products/{id}/reduce-stock?quantity=N` | Reduce stock (called internally by Order Service) |

### Order Service (`localhost:8081`)

| Method | Endpoint          | Description        |
|--------|--------------------|----------------------|
| POST   | `/orders`          | Place a new order    |
| GET    | `/orders`          | List all orders      |
| GET    | `/orders/{id}`      | Get an order by ID   |
| DELETE | `/orders/{id}`      | Cancel an order      |

## 6. Testing the APIs

### Add a product
```bash
curl -X POST http://localhost:8080/products \
  -H "Content-Type: application/json" \
  -d '{"name":"Wireless Mouse","price":799.0,"quantity":50}'
```

### View all products
```bash
curl http://localhost:8080/products
```

### Get a product by ID
```bash
curl http://localhost:8080/products/1
```

### Place an order (Order Service internally calls Product Service)
```bash
curl -X POST http://localhost:8081/orders \
  -H "Content-Type: application/json" \
  -d '{"productId":1,"customerName":"Rahul Sharma","quantity":2}'
```

What happens behind the scenes:
1. Order Service receives the request.
2. Order Service calls `GET http://localhost:8080/products/1` to fetch product details.
3. It checks the product exists and enough stock is available.
4. It calculates `totalPrice = price * quantity` and saves the order.
5. It calls `PUT http://localhost:8080/products/1/reduce-stock?quantity=2` to reduce stock.

### View all orders
```bash
curl http://localhost:8081/orders
```

### Cancel an order
```bash
curl -X DELETE http://localhost:8081/orders/1
```

## 7. Notes

- Both databases are **in-memory (H2)** — all data resets when a service restarts.
- If Product Service is down or unreachable, Order Service will fail to place new orders
  since it depends on a live call to fetch product details and reduce stock.

## 8. Suggested Next Steps (Optional Enhancements)

- Swap `RestTemplate` for `WebClient` or **OpenFeign** for cleaner inter-service calls.
- Add **Eureka** service discovery so services find each other by name instead of
  hardcoded `localhost` URLs.
- Add an **API Gateway** (Spring Cloud Gateway) in front of both services.
- Add **Spring Security + JWT** for authenticated endpoints.
- Add **Swagger/OpenAPI** (`springdoc-openapi-starter-webmvc-ui`) for interactive API docs.
- Switch from H2 to a persistent database (MySQL/PostgreSQL) for production use, and
  containerize both services with **Docker** + `docker-compose` for one-command startup.
- Add a resilience layer (Resilience4j circuit breaker) around the Product Service call
  so Order Service degrades gracefully if Product Service is down.
