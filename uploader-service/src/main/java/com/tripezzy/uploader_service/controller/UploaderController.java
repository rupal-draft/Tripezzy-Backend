package com.tripezzy.uploader_service.controller;

import com.tripezzy.uploader_service.service.FileUploaderService;
import io.github.resilience4j.ratelimiter.annotation.RateLimiter;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/core")
public class UploaderController {

    private final FileUploaderService fileUploaderService;

    public UploaderController(FileUploaderService fileUploaderService) {
        this.fileUploaderService = fileUploaderService;
    }

    @PostMapping("/public")
    @RateLimiter(name = "uploader", fallbackMethod = "uploadFilefallback")
    public ResponseEntity<Map<String, String>> uploadFile(@RequestParam MultipartFile file) {
        return ResponseEntity.ok(fileUploaderService.uploadFile(file));
    }

    @DeleteMapping("/public")
    @RateLimiter(name = "uploader", fallbackMethod = "deleteFilefallback")
    public ResponseEntity<Boolean> deleteFile(@RequestParam String publicId) {
        return ResponseEntity.ok(fileUploaderService.deleteFile(publicId));
    }

    public ResponseEntity<Map<String, String>> uploadFileFallback(MultipartFile file, Throwable t) {
        Map<String, String> response = new HashMap<>();
        response.put("error", "Too many upload requests. Please try again later.");
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(response);
    }

    public ResponseEntity<Boolean> deleteFileFallback(String publicId, Throwable t) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(false);
    }
}
