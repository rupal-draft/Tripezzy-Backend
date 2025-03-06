# Blog Service Microservice

This repository contains the code for a Blog Service microservice, built using Spring Boot. This service provides functionalities for creating, managing, and retrieving blog posts, along with features for likes and comments.

## Features

* **Blog Management:**
    * Create, update, and delete blog posts.
    * Retrieve blogs by ID, author, or status.
    * Search and filter blogs.
    * Soft delete functionality
* **Likes and Comments:**
    * Add and remove likes from blogs.
    * Add, update, and delete comments on blogs.
    * Retrieve likes and comments for a blog.
* **Rate Limiting:**
    * Implemented rate limiting using Resilience4j to prevent abuse.
* **Database:**
    * Uses a relational database (configured via environment variables).
* **Auditing:**
    * Created and updated timestamps are automatically managed.

## Technologies Used

* Spring Boot
* Spring Data JPA
* Resilience4j (for rate limiting)
* PostgresQL
* Hibernate
* Validation API

## Entities and Database Schemas

The service uses the following entities, which translate to database schemas:

### `Blog`

| Column        | Data Type        | Constraints             | Description                                                                 |
|---------------|------------------|-------------------------|-----------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the blog post.                                       |
| `title`       | `VARCHAR(200)`   | `NOT NULL`              | Title of the blog post.                                                      |
| `content`     | `TEXT`           | `NOT NULL`              | Content of the blog post.                                                    |
| `author_id`   | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the author of the blog post.                                           |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the blog post was created.                                    |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the blog post was last updated.                                |
| `status`      | `VARCHAR(255)`   | `NOT NULL`, `INDEX`     | Status of the blog post (e.g., `DRAFT`, `PUBLISHED`).                         |
| `category`    | `VARCHAR(255)`   | `NOT NULL`, `INDEX`     | Category of the blog post.                                                   |
| `tag`         | `VARCHAR(255)`   | `NOT NULL`, `INDEX`     | Tags associated with the blog post.                                           |
| `deleted`     | `BOOLEAN`        | `DEFAULT FALSE`         | Soft delete flag.                                                              |

### `Comment`

| Column        | Data Type        | Constraints             | Description                                                                 |
|---------------|------------------|-------------------------|-----------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the comment.                                         |
| `content`     | `VARCHAR(500)`   | `NOT NULL`              | Content of the comment.                                                       |
| `user_id`     | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the user who made the comment.                                         |
| `blog_id`     | `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the blog post the comment belongs to.                               |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the comment was created.                                     |
| `updated_at`  | `TIMESTAMP`      |                         | Timestamp when the comment was last updated.                                |

### `Like`

| Column        | Data Type        | Constraints             | Description                                                                 |
|---------------|------------------|-------------------------|-----------------------------------------------------------------------------|
| `id`          | `BIGINT`         | `PRIMARY KEY`, `AUTO_INCREMENT` | Unique identifier for the like.                                            |
| `user_id`     | `BIGINT`         | `NOT NULL`, `INDEX`     | ID of the user who liked the blog post.                                      |
| `blog_id`     | `BIGINT`         | `NOT NULL`, `FOREIGN KEY`, `INDEX` | ID of the blog post the like belongs to.                                  |
| `created_at`  | `TIMESTAMP`      | `NOT NULL`              | Timestamp when the like was created.                                        |

## API Endpoints

The following table lists the API endpoints provided by the service:

| Method | Endpoint                    | Description                                                                                                                                                                                             |
|--------|-----------------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `POST` | `/blogs`                    | Creates a new blog post.                                                                                                                                                                               |
| `GET`  | `/blogs/{blogId}`           | Retrieves a blog post by its ID.                                                                                                                                                                       |
| `GET`  | `/blogs`                    | Retrieves all blog posts (paginated).                                                                                                                                                                 |
| `GET`  | `/blogs/author/{authorId}`  | Retrieves blog posts by author ID (paginated).                                                                                                                                                         |
| `GET`  | `/blogs/status/{status}`    | Retrieves blog posts by status (paginated).                                                                                                                                                             |
| `PUT`  | `/blogs/{blogId}`           | Updates a blog post.                                                                                                                                                                                   |
| `PUT`  | `/blogs/{blogId}/status/{status}` | Updates the status of a blog post.                                                                                                                                                                 |
| `DELETE`| `/blogs/{blogId}`           | Deletes a blog post.                                                                                                                                                                                   |
| `POST` | `/blogs/{blogId}/likes`     | Adds a like to a blog post.                                                                                                                                                                             |
| `DELETE`| `/blogs/{blogId}/likes/{likeId}` | Removes a like from a blog post.                                                                                                                                                                 |
| `POST` | `/blogs/{blogId}/comments`  | Adds a comment to a blog post.                                                                                                                                                                         |
| `PUT`  | `/blogs/{blogId}/comments/{commentId}` | Updates a comment on a blog post.                                                                                                                                                               |
| `DELETE`| `/blogs/{blogId}/comments/{commentId}` | Deletes a comment from a blog post.                                                                                                                                                           |
| `GET`  | `/blogs/{blogId}/likes`     | Retrieves all likes for a blog post.                                                                                                                                                                  |
| `GET`  | `/blogs/{blogId}/comments`  | Retrieves all comments for a blog post.                                                                                                                                                               |
| `GET`  | `/blogs/search`             | Searches blog posts based on a query (paginated).                                                                                                                                                     |
| `GET`  | `/blogs/filter`             | Filters blog posts based on category and tags (paginated).                                                                                                                                              |
| `DELETE`| `/blogs/soft-delete/{blogId}`| Soft deletes a blog post.                                                                                                                                                                              |
| `GET` | `/blogs/filter/advanced`    | Filters blog posts based on authorId, status, category and tags (paginated).|


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
      blogRateLimiter:
        limitForPeriod: 10 # Number of requests allowed per period
        limitRefreshPeriod: 10s # Period in seconds for refreshing limits
        timeoutDuration: 1s # Timeout for acquiring a permission