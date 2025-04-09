package com.tripezzy.uploader_service.service.implementations;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.cloudinary.utils.StringUtils;
import com.tripezzy.uploader_service.exceptions.*;
import com.tripezzy.uploader_service.service.FileUploaderService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

@Service
public class CloudinaryFileUploaderService implements FileUploaderService {


    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/gif",
            "image/webp",
            "image/svg+xml",
            "application/pdf"
    );

    private static final long MAX_FILE_SIZE = 10 * 1024 * 1024; // 10MB
    private static final Logger log = LoggerFactory.getLogger(CloudinaryFileUploaderService.class);

    private final Cloudinary cloudinary;

    public CloudinaryFileUploaderService(Cloudinary cloudinary) {
        this.cloudinary = Objects.requireNonNull(cloudinary, "Cloudinary client must not be null");
    }

    @Override
    public Map<String, String> uploadFile(MultipartFile file) {
        validateFile(file);
        log.info("Uploading file: {}", file.getOriginalFilename());
        try {
            Map<String, Object> uploadOptions = new HashMap<>();
            uploadOptions.put("resource_type", determineResourceType(file.getContentType()));

            Map<?, ?> uploadResult = cloudinary.uploader()
                    .upload(file.getBytes(), uploadOptions);
            log.info("Upload result: {}", uploadResult.get("public_id"));
            validateUploadResult(uploadResult);
            log.info("Successfully uploaded file: {}", file.getOriginalFilename());
            return Map.of(
                    "url", uploadResult.get("secure_url").toString(),
                    "public_id", uploadResult.get("public_id").toString()
            );
        } catch (IOException e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new FileUploadException("Failed to upload file: " + file.getOriginalFilename(), e);
        } catch (ServiceUnavailable e) {
            log.error("Failed to upload file: {}", file.getOriginalFilename(), e);
            throw new ServiceUnavailable("Failed to upload file: " + file.getOriginalFilename());
        }
    }

    @Override
    public boolean deleteFile(String publicId) {
        if (StringUtils.isBlank(publicId)) {
            throw new IllegalState("Public ID cannot be null or empty");
        }
        log.info("Deleting file with publicId: {}", publicId);
        try {
            Map<?, ?> result = cloudinary.uploader()
                    .destroy(publicId, ObjectUtils.emptyMap());

            validateDeleteResult(result);
            log.info("Successfully deleted file with publicId: {}", publicId);
            String status = (String) result.get("result");
            return "ok".equals(status) || "not_found".equals(status);
        } catch (IOException e) {
            log.error("Failed to delete file with publicId: {}", publicId, e);
            throw new FileDeletionException("Failed to delete file with publicId: " + publicId, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalState("File must not be null or empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new FileSizeExceededException(
                    String.format("File size exceeds the limit of %d MB", MAX_FILE_SIZE / (1024 * 1024))
            );
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType)) {
            throw new UnsupportedFileTypeException(
                    String.format("File type %s is not supported", contentType)
            );
        }
    }

    private String determineResourceType(String contentType) {
        if (contentType == null) {
            return "auto";
        }

        return contentType.startsWith("image/") ? "image" : "raw";
    }

    private void validateUploadResult(Map<?, ?> uploadResult) {
        if (uploadResult == null) {
            throw new FileUploadException("Cloudinary returned null response");
        }

        if (!uploadResult.containsKey("secure_url") || !uploadResult.containsKey("public_id")) {
            log.error("Incomplete upload response: {}", uploadResult);
            throw new FileUploadException("Cloudinary returned incomplete response");
        }
    }

    private void validateDeleteResult(Map<?, ?> deleteResult) {
        if (deleteResult == null) {
            throw new FileDeletionException("Cloudinary returned null response");
        }

        if (!deleteResult.containsKey("result")) {
            log.error("Incomplete delete response: {}", deleteResult);
            throw new FileDeletionException("Cloudinary returned incomplete response");
        }
    }

}
