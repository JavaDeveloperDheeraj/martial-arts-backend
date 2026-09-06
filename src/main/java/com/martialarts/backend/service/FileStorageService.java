package com.martialarts.backend.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.UUID;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public void validateFile(MultipartFile file, String expectedType) {
        if (file == null || file.isEmpty()) {
            throw new RuntimeException("File is required and cannot be empty.");
        }

        long sizeInKB = file.getSize() / 1024;
        String contentType = file.getContentType();

        if ("IMAGE".equalsIgnoreCase(expectedType)) {
            if (contentType == null || (!contentType.equalsIgnoreCase("image/jpeg") 
                    && !contentType.equalsIgnoreCase("image/jpg") 
                    && !contentType.equalsIgnoreCase("image/png"))) {
                throw new RuntimeException("Invalid image format! Only JPG, JPEG, and PNG files are allowed.");
            }
            if (sizeInKB < 20 || sizeInKB > 100) {
                throw new RuntimeException("Image size must be between 20 KB and 100 KB. (Received: " + sizeInKB + " KB)");
            }
        } else if ("PDF".equalsIgnoreCase(expectedType)) {
            if (contentType == null || !contentType.equalsIgnoreCase("application/pdf")) {
                throw new RuntimeException("Invalid document format! Only PDF files are allowed.");
            }
            if (sizeInKB < 50 || sizeInKB > 200) {
                throw new RuntimeException("PDF size must be between 50 KB and 200 KB. (Received: " + sizeInKB + " KB)");
            }
        }
    }

    public String storeFile(MultipartFile file, String prefix) throws IOException {
        File directory = new File(uploadDir);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String originalName = file.getOriginalFilename() != null 
                ? file.getOriginalFilename().replaceAll("\\s+", "_") 
                : "document";

        String finalFileName = prefix + "_" + UUID.randomUUID() + "_" + originalName;
        File targetFile = new File(directory, finalFileName);

        try (FileOutputStream fos = new FileOutputStream(targetFile)) {
            fos.write(file.getBytes());
        }

        return targetFile.getAbsolutePath();
    }
}