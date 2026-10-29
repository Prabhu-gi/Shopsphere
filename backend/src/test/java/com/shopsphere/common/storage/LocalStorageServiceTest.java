package com.shopsphere.common.storage;

import com.shopsphere.common.exception.BadRequestException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Comparator;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LocalStorageServiceTest {

    private LocalStorageService storageService;

    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        storageService = new LocalStorageService();
        ReflectionTestUtils.setField(storageService, "baseUploadDir", tempDir.toString());
    }

    @Test
    @DisplayName("store should successfully save image with valid extension")
    void givenValidImage_whenStore_thenReturnsPath() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test-product.png",
                "image/png",
                "image content".getBytes()
        );

        String result = storageService.store(file, "products");

        assertThat(result).isNotNull();
        assertThat(result).startsWith("/uploads/products/");
        assertThat(result).endsWith(".png");
    }

    @Test
    @DisplayName("store should reject empty file")
    void givenEmptyFile_whenStore_thenThrowsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.png",
                "image/png",
                new byte[0]
        );

        assertThatThrownBy(() -> storageService.store(file, "products"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("cannot be empty");
    }

    @Test
    @DisplayName("store should reject disallowed file extension")
    void givenDisallowedExtension_whenStore_thenThrowsBadRequest() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "script.exe",
                "application/octet-stream",
                "malicious".getBytes()
        );

        assertThatThrownBy(() -> storageService.store(file, "products"))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("Invalid file format");
    }

    @Test
    @DisplayName("delete should remove existing file")
    void givenStoredFile_whenDelete_thenFileIsDeleted() throws IOException {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "photo.jpg",
                "image/jpeg",
                "photo-data".getBytes()
        );

        String fileUrl = storageService.store(file, "products");
        storageService.delete(fileUrl);

        // Subdirectory file should no longer exist
        String relative = fileUrl.substring("/uploads/".length());
        Path filePath = tempDir.resolve(relative);
        assertThat(Files.exists(filePath)).isFalse();
    }
}

