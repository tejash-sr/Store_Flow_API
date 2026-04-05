package com.storeflow.storeflow_api.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for FileStorageService
 */
class FileStorageServiceTest {

    private FileStorageService fileStorageService;
    private String testStoragePath;

    @BeforeEach
    void setUp() throws IOException {
        fileStorageService = new FileStorageService();
        testStoragePath = "test-uploads-" + System.currentTimeMillis();
        ReflectionTestUtils.setField(fileStorageService, "baseStoragePath", testStoragePath);
        
        // Create test directory
        Files.createDirectories(Paths.get(testStoragePath));
    }

    @Test
    void testValidateFile_ValidImageFile_Success() {
        // Create a valid JPEG file
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8}; // JPEG magic bytes
        MultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);

        // Should not throw
        assertDoesNotThrow(() -> {
            fileStorageService.saveProductImage(file, "1");
        });
    }

    @Test
    void testValidateFile_FileTooLarge_ThrowsException() {
        // Create a file larger than 5MB
        byte[] content = new byte[6 * 1024 * 1024 + 1];
        MultipartFile file = new MockMultipartFile(
            "file", "large.jpg", "image/jpeg", content);

        // Should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.saveProductImage(file, "1");
        });
    }

    @Test
    void testValidateFile_InvalidMimeType_ThrowsException() {
        // Create a file with invalid MIME type
        byte[] content = "invalid content".getBytes();
        MultipartFile file = new MockMultipartFile(
            "file", "test.txt", "text/plain", content);

        // Should throw IllegalArgumentException
        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.saveProductImage(file, "1");
        });
    }

    @Test
    void testValidateFile_EmptyFile_ThrowsException() {
        MultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", new byte[0]);

        assertThrows(IllegalArgumentException.class, () -> {
            fileStorageService.saveProductImage(file, "1");
        });
    }

    @Test
    void testSaveProductImage_ValidFile_ReturnFilePath() throws IOException {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        MultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);

        String filePath = fileStorageService.saveProductImage(file, "123");

        // Verify file path format
        assertNotNull(filePath);
        assertTrue(filePath.startsWith("products/123/"));
        assertTrue(filePath.endsWith(".jpg"));
    }

    @Test
    void testSaveUserAvatar_ValidFile_ReturnFilePath() throws IOException {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        MultipartFile file = new MockMultipartFile(
            "file", "avatar.png", "image/png", content);

        String filePath = fileStorageService.saveUserAvatar(file, "user@example.com");

        // Verify file path format
        assertNotNull(filePath);
        assertTrue(filePath.startsWith("avatars/user@example.com/"));
        assertTrue(filePath.endsWith(".png"));
    }

    @Test
    void testLoadFile_FileExists_ReturnFileBytes() throws IOException {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};
        MultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);

        String filePath = fileStorageService.saveProductImage(file, "456");

        // Load the file
        byte[] loadedBytes = fileStorageService.loadFile(filePath);

        assertNotNull(loadedBytes);
        assertArrayEquals(content, loadedBytes);
    }

    @Test
    void testLoadFile_FileNotFound_ThrowsIOException() {
        assertThrows(IOException.class, () -> {
            fileStorageService.loadFile("nonexistent/path.jpg");
        });
    }

    @Test
    void testDeleteFile_FileExists_FileDeleted() throws IOException {
        byte[] content = new byte[]{(byte) 0xFF, (byte) 0xD8};
        MultipartFile file = new MockMultipartFile(
            "file", "test.jpg", "image/jpeg", content);

        String filePath = fileStorageService.saveProductImage(file, "789");
        Path fullPath = Paths.get(testStoragePath, filePath);
        assertTrue(Files.exists(fullPath), "File should exist after save");

        fileStorageService.deleteFile(filePath);

        assertFalse(Files.exists(fullPath), "File should be deleted");
    }

    @Test
    void testGetContentType_ValidContentType_ReturnSame() {
        String result = fileStorageService.getContentType("image/jpeg");
        assertEquals("image/jpeg", result);
    }

    @Test
    void testGetContentType_NullContentType_ReturnDefault() {
        String result = fileStorageService.getContentType(null);
        assertEquals("image/jpeg", result);
    }

}
