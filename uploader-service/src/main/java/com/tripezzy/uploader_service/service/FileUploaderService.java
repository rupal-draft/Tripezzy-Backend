package com.tripezzy.uploader_service.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

public interface FileUploaderService {

    Map<String, String> uploadFile(MultipartFile file);

    boolean deleteFile(String publicId);
}
