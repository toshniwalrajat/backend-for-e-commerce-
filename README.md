# E-commerce Backend

A backend-only e-commerce REST API built with Spring Boot, Spring Data JPA, and MySQL. It supports product catalog management, anonymous shopping carts, checkout, mock payments, and inventory updates.

> This project does not include a frontend, user accounts, or authentication. Payment methods are simulated; no real payments are processed.

## Technology

- Java 17+
- Spring Boot
- Spring Web
- Spring Data JPA
- MySQL
- Jakarta Bean Validation
- Maven

## Features

- Manage products, categories, and brands
- Add products to anonymous carts using a cart token
- Update quantities and remove cart items
- Validate product availability and stock
- Check out a cart and create an order
- Save order-item details and prices as purchase-time snapshots
- Simulate payment methods: `MOCK_CARD`, `MOCK_BANK_TRANSFER`, and `CASH_ON_DELIVERY`

## Requirements

- JDK 17 or newer
- MySQL Server
- Git

## Getting started

### 1. Clone the repository

```bash
git clone https://github.com/YOUR_USERNAME/YOUR_REPOSITORY.git
cd YOUR_REPOSITORY
```

### 2. Create a MySQL database

```sql
CREATE DATABASE ecommerce_db;
```

### 3. Configure database credentials

In `src/main/resources/application.properties`, configure the connection:

```properties
spring.datasource.url=${DB_URL:jdbc:mysql://localhost:3306/ecommerce_db}
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}

spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true
```

Set the `DB_PASSWORD` environment variable before starting the application. For example, in Windows PowerShell:

```powershell
$env:DB_PASSWORD="your_mysql_password"
```

On macOS/Linux:

```bash
export DB_PASSWORD="your_mysql_password"
```

Do not commit real passwords or other secrets to GitHub.

### 4. Run the application

On Windows:

```powershell
.\mvnw.cmd spring-boot:run
```

On macOS/Linux:

```bash
./mvnw spring-boot:run
```

The API runs at:

```text
http://localhost:8080
```

## API overview

All endpoints use the base URL `http://localhost:8080`.

### Products

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/products` | List products |
| `GET` | `/api/products/{id}` | Get a product |
| `POST` | `/api/products` | Create a product |
| `PUT` | `/api/products/{id}` | Update a product |
| `DELETE` | `/api/products/{id}` | Delete a product |

Example product request:

```http
POST /api/products
Content-Type: application/json
```

```json
{
  "name": "Wireless Mouse",
  "description": "Ergonomic wireless mouse",
  "sku": "MOUSE-001",
  "price": 29.99,
  "stockQuantity": 10,
  "active": true,
  "category": {
    "id": 1
  },
  "brand": {
    "id": 1
  }
}
```

Create the category and brand first, then use their returned IDs in the product request.

### Categories

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/categories` | List categories |
| `GET` | `/api/categories/{id}` | Get a category |
| `POST` | `/api/categories` | Create a category |
| `PUT` | `/api/categories/{id}` | Update a category |
| `DELETE` | `/api/categories/{id}` | Delete a category |

Example:

```json
{
  "name": "Electronics",
  "description": "Electronic devices and accessories",
  "active": true
}
```

### Brands

| Method | Endpoint | Description |
|---|---|---|
| `GET` | `/api/brands` | List brands |
| `GET` | `/api/brands/{id}` | Get a brand |
| `POST` | `/api/brands` | Create a brand |
| `PUT` | `/api/brands/{id}` | Update a brand |
| `DELETE` | `/api/brands/{id}` | Delete a brand |

Example:

```json
{
  "name": "Acme",
  "description": "Example product brand",
  "active": true
}
```

### Carts

Create a cart first. The response contains a `cartToken`; use its actual value in subsequent URLs.

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/carts` | Create an anonymous cart |
| `GET` | `/api/carts/{cartToken}` | View the cart |
| `POST` | `/api/carts/{cartToken}/items` | Add a product |
| `PATCH` | `/api/carts/{cartToken}/items/{productId}` | Change item quantity |
| `DELETE` | `/api/carts/{cartToken}/items/{productId}` | Remove an item |

Add a product to a cart:

```http
POST /api/carts/YOUR_CART_TOKEN/items
Content-Type: application/json
```

```json
{
  "productId": 1,
  "quantity": 2
}
```

Update quantity:

```http
PATCH /api/carts/YOUR_CART_TOKEN/items/1
Content-Type: application/json
```

```json
{
  "quantity": 3
}
```

### Orders and checkout

| Method | Endpoint | Description |
|---|---|---|
| `POST` | `/api/orders/checkout/{cartToken}` | Check out a cart |
| `GET` | `/api/orders/{orderNumber}` | Get an order |

Checkout example:

```http
POST /api/orders/checkout/YOUR_CART_TOKEN
Content-Type: application/json
```

```json
{
  "paymentMethod": "MOCK_CARD"
}
```

Supported payment methods:

- `MOCK_CARD`
- `MOCK_BANK_TRANSFER`
- `CASH_ON_DELIVERY`

The mock card and bank transfer methods simulate a successful payment. Cash on delivery leaves the payment pending. **No real payment is processed.**

## Testing with Postman

A typical test flow is:

1. Create a category and note its ID.
2. Create a brand and note its ID.
3. Create a product using those IDs; note its product ID.
4. Create a cart and copy its `cartToken`.
5. Add the product to the cart.
6. Check out the cart.
7. Retrieve the order using its `orderNumber`.
8. Check the product to confirm that stock was reduced.

When using URL examples, replace placeholders such as `YOUR_CART_TOKEN` with actual returned values; do not include the placeholder braces.

## Notes

- JPA's `ddl-auto=update` is convenient for local development. For production, use a database migration tool such as Flyway or Liquibase.
- Cart tokens are anonymous identifiers, not authentication or authorization.
- Stock is checked during cart operations and checkout. This project is intended for learning; production inventory handling needs stronger concurrency protection.
- Never store card numbers, CVVs, or other sensitive payment information in this application.

## License

Add a license if you plan to distribute or reuse this project.
