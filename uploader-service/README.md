# Uploader Service API Documentation

This documentation outlines the available API endpoints for the Uploader Service, which handles file uploads to and deletions from Cloudinary. This service does not persist data in its own database.

## Features

* **File Upload:**
    * Accepts multipart files and uploads them to Cloudinary.
    * Returns the public ID and URL of the uploaded file.
* **File Deletion:**
    * Accepts a Cloudinary public ID and deletes the corresponding file.
    * Returns a boolean indicating the success of the deletion.
* **Rate Limiting:**
    * Implemented rate limiting using Resilience4j to prevent abuse.
* **Cloudinary Integration:**
    * Leverages the Cloudinary Java SDK for file management.

## Technologies Used

* Spring Boot
* Spring Web
* Resilience4j (for rate limiting)
* Cloudinary Java SDK
* Cloudinary API
* Validation API

## API Endpoints

| Method   | Endpoint | Request Body          | Description                                                                                                                                                                                             | Response Body                       |
|----------|----------|-----------------------|---------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------------|-------------------------------------|
| `POST`   | `/core`  | `multipart/form-data` (`file` parameter) | Uploads the provided file to Cloudinary.                                                                                                                                                | `200 OK`: `{"public_id": "...", "url": "..."}` <br> `429 Too Many Requests`: `{"error": "Too many upload requests. Please try again later."}` |
| `DELETE` | `/core`  | `application/x-www-form-urlencoded` (`publicId` parameter) | Deletes the file with the specified Cloudinary public ID.                                                                                                              | `200 OK`: `true` (if deletion successful) <br> `429 Too Many Requests`: `false`                                                                   |

## Configuration

The service is configured using `application.properties` or `application.yml` (environment variable based) to connect to Cloudinary.

**Example `application.properties` configuration:**

```properties
cloudinary.cloud-name=<span class="math-inline">\{CLOUDINARY\_CLOUD\_NAME\}
cloudinary\.api\-key\=</span>{CLOUDINARY_API_KEY}
cloudinary.api-secret=${CLOUDINARY_API_SECRET}
```

## Cloudinary Configuration Class:

```java
@Configuration
public class UploaderConfig {

    @Value("<span class="math-inline">\{cloudinary\.cloud\-name\}"\)
    private String cloudName;
    
    @Value("</span>{cloudinary.api-key}")
    private String apiKey;

    @Value("${cloudinary.api-secret}")
    private String apiSecret;

    @PostConstruct
    public void validateUrl() {
        if (cloudName == null || apiKey == null || apiSecret == null) {
            throw new IllegalState("Cloudinary credentials not found.");
        }
    }

    @Bean
    public Cloudinary cloudinary() {

        Map<String, String> configMap = Map.of(
                "cloud_name", cloudName,
                "api_key", apiKey,
                "api_secret", apiSecret
        );

        return new Cloudinary(configMap);
    }
}
```

## Important Notes
1.This service relies on a properly configured Cloudinary account.
2.Ensure your Cloudinary credentials are secure and not exposed.
3.The /core endpoint handles both upload and delete operations based on the HTTP method used.