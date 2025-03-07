# Ecommerce Service Microservice

This directory contains the code for an Ecommerce Service microservice, built using Spring Boot. This service provides functionalities for managing products, user carts, and wishlists.

## Features

* **Product Management:**
    * Create, update, and delete products.
    * Retrieve products by ID.
    * Search and filter products.
    * Soft delete functionality
* **Cart Management:**
    * Add and remove items from a user's cart.
    * Retrieve a user's cart.
    * Calculate the total cost of a cart, including discounts using a strategy design pattern.
* **Wishlist Management:**
    * Add and remove products from a user's wishlist.
    * Retrieve a user's wishlist.
* **Rate Limiting:**
    * Implemented rate limiting using Resilience4j to prevent abuse.
* **Database:**
    * Uses a relational database (configured via environment variables).
* **Auditing:**
    * Created and updated timestamps are automatically managed.
* **Discount Strategy:**
    * Implementation of the Strategy design pattern for applying various discount types based on quantity and price.

## Technologies Used

* Spring Boot
* Spring Data JPA
* Resilience4j (for rate limiting)
* Lombok
* Hibernate
* Validation API

## Entities and Database Schemas

The service uses the following entities, which translate to database schemas:

### `Product`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the product.                                           |
| `name`        | `VARCHAR(200)`   | `NOT NULL`              | Name of the product.                                                          |
| `description` | `TEXT`           | `NOT NULL`              | Description of the product.                                                   |
| `price`       | `DOUBLE`         | `NOT NULL`              | Price of the product.                                                         |
| `stock`       | `INTEGER`        | `NOT NULL`              | Current stock of the product.                                                 |
| `category`    | `VARCHAR(255)`   | `NOT NULL`              | Category of the product.                                                      |
| `imageUrl`    | `VARCHAR(255)`   | `NOT NULL`              | URL of the product's image.                                                   |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the product was created.                                       |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the product was last updated.                                   |
| `deleted`     | `BOOLEAN`        | `DEFAULT FALSE`         | Soft delete flag.                                                               |

### `Cart`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the cart.                                            |
| `user_id`     | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the user associated with the cart.                                      |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the cart was created.                                         |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the cart was last updated.                                     |

### `CartItem`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the cart item.                                       |
| `cart_id`     | `BIGINT`         | `NOT NULL`, `FOREIGN KEY` | ID of the cart the item belongs to.                                         |
| `product_id`  | `BIGINT`         | `NOT NULL`, `FOREIGN KEY` | ID of the product in the cart item.                                         |
| `quantity`    | `INTEGER`        | `NOT NULL`              | Quantity of the product in the cart item.                                   |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the cart item was created.                                    |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the cart item was last updated.                                |

### `Wishlist`

| Column        | Data Type        | Constraints             | Description                                                                  |
|---------------|------------------|-------------------------|------------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the wishlist item.                                   |
| `user_id`     | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the user associated with the wishlist.                                  |
| `product_id`  | `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the product in the wishlist.                                       |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the wishlist item was created.                               |

## API Endpoints

### Product Controller

| Method | Endpoint              | Description                                                                                                                                                                                              |
|--------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/products`           | Creates a new product.                                                                                                                                                                                  |
| `GET`  | `/products/{productId}` | Retrieves a product by its ID.                                                                                                                                                                          |
| `GET`  | `/products`           | Retrieves all products (paginated).                                                                                                                                                                      |
| `PUT`  | `/products/{productId}` | Updates a product.                                                                                                                                                                                      |
| `DELETE`| `/products/{productId}` | Deletes a product.                                                                                                                                                                                      |
| `GET`  | `/products/filter`    | Filters products based on category, minimum price, and maximum price (paginated).                                                                                                                          |
| `DELETE`| `/products/soft-delete/{productId}` | Soft deletes a product.                                                                                                                                                                     |
| `GET` | `/products/search` | Searches products based on a query (paginated).                                                                                                                                                           |

### Cart Controller

| Method | Endpoint                  | Description                                                                                                                                                                                          |
|--------|---------------------------|------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/carts/{userId}/items`   | Adds an item to a user's cart.                                                                                                                                                                       |
| `GET`  | `/carts/{userId}`         | Retrieves a user's cart.                                                                                                                                                                            |
| `DELETE`| `/carts/{userId}/items/{productId}` | Removes an item from a user's cart.                                                                                                                                                       |
| `GET`  | `/carts/{userId}/total-cost` | Calculates the total cost of a user's cart, with optional discount parameters.                                                                                                                   |

### Wishlist Controller

| Method | Endpoint              | Description                                                                                                                                                                                              |
|--------|-----------------------|----------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/wishlist`           | Adds a product to a user's wishlist.                                                                                                                                                                    |
| `GET`  | `/wishlist/{userId}`  | Retrieves a user's wishlist (paginated).                                                                                                                                                                 |
| `DELETE`| `/wishlist/{wishlistId}` | Removes a product from a user's wishlist.                                                                                                                                                               |

## Configuration

The service is configured using `application.properties`. The following properties are used:

| Property                              | Description                                                                  |
|---------------------------------------|------------------------------------------------------------------------------|
| `spring.application.name`             | The name of the Spring application.                                          |
| `deploy.env`                          | The deployment environment.                                                  |
| `server.port`                         | The port on which the service runs.                                           |
| `spring.datasource.url`              | The URL of the database.                                                    |
| `spring.datasource.username`         | The username for the database.                                              |
| `spring.datasource.password`         | The password for the database.                                              |
| `spring.jpa.hibernate.ddl-auto`       | Hibernate DDL auto strategy.                                                |
| `spring.jpa.show-sql`                | Whether to show SQL queries in the console.                                |
| `spring.jpa.properties.hibernate.format_sql` | Whether to format SQL queries in the console.                                |

## Rate Limiter configuration.

The Rate limiter is set with these parameters.
```yaml
resilience4j:
  ratelimiter:
    instances:
      productRateLimiter:
        limitForPeriod: 10 # Number of requests allowed per period
        limitRefreshPeriod: 10s # Period in seconds for refreshing limits
        timeoutDuration: 1s # Timeout for acquiring a permission
      cartRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
      wishlistRateLimiter:
        limitForPeriod: 10
        limitRefreshPeriod: 10s
        timeoutDuration: 1s
```  

## Discount Strategy Pattern.

This microservice implements the Strategy design pattern for applying discounts to the cart's total cost. This allows for flexible and extensible discount logic. Different discount strategies can be added without modifying the core cart calculation logic.