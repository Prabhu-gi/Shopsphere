package com.shopsphere.common.storage;

import com.shopsphere.common.exception.BadRequestException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.*;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
public class LocalStorageService implements FileStorageService {

    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "webp");
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB

    @Value("${app.upload.dir:uploads}")
    private String baseUploadDir;

    @Override
    public String store(MultipartFile file, String subdirectory) {
        if (file == null || file.isEmpty()) {
            throw new BadRequestException("Uploaded file cannot be empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new BadRequestException("File size exceeds maximum limit of 5MB");
        }

        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename() != null ? file.getOriginalFilename() : "");
        String extension = getFileExtension(originalFilename).toLowerCase();

        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new BadRequestException("Invalid file format. Allowed formats: " + String.join(", ", ALLOWED_EXTENSIONS));
        }

        String subDirName = (subdirectory != null && !subdirectory.isBlank()) ? subdirectory.trim() : "general";
        Path targetDir = Paths.get(baseUploadDir, subDirName).toAbsolutePath().normalize();

        try {
            Files.createDirectories(targetDir);
            String newFilename = UUID.randomUUID() + "." + extension;
            Path targetFile = targetDir.resolve(newFilename);

            try (InputStream inputStream = file.getInputStream()) {
                Files.copy(inputStream, targetFile, StandardCopyOption.REPLACE_EXISTING);
            }

            log.info("Stored file successfully: {}", targetFile);
            return "/uploads/" + subDirName + "/" + newFilename;
        } catch (IOException e) {
            log.error("Failed to store file", e);
            throw new RuntimeException("Could not store file: " + e.getMessage(), e);
        }
    }

    @Override
    public void delete(String fileUrl) {
        if (fileUrl == null || fileUrl.isBlank()) {
            return;
        }

        try {
            String relativePath = fileUrl.startsWith("/uploads/") ? fileUrl.substring("/uploads/".length()) : fileUrl;
            Path filePath = Paths.get(baseUploadDir).resolve(relativePath).toAbsolutePath().normalize();
            Files.deleteIfExists(filePath);
            log.info("Deleted file: {}", filePath);
        } catch (IOException e) {
            log.warn("Could not delete file: {}", fileUrl, e);
        }
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex > 0 && dotIndex < filename.length() - 1) {
            return filename.substring(dotIndex + 1);
        }
        return "";
    }
}

