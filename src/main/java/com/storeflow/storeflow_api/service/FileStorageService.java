package com.storeflow.storeflow_api.service;

import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Service
@Slf4j
public class FileStorageService {

    @Value("${storage.base-path:uploads}")
    private String baseStoragePath;

    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final Set<String> ALLOWED_IMAGE_TYPES = new HashSet<>();

    static {
        ALLOWED_IMAGE_TYPES.add("image/jpeg");
        ALLOWED_IMAGE_TYPES.add("image/png");
        ALLOWED_IMAGE_TYPES.add("image/webp");
    }

    /**
     * Save product image file
     * @param file MultipartFile to save
     * @param productId Product ID for file organization
     * @return relative path to saved file
     */
    public String saveProductImage(MultipartFile file, String productId) throws IOException {
        validateFile(file);
        return saveFile(file, "products", productId);
    }

    /**
     * Save user avatar file with automatic resizing to standard dimensions
     * @param file MultipartFile to save
     * @param userId User ID for file organization
     * @return relative path to saved file
     */
    public String saveUserAvatar(MultipartFile file, String userId) throws IOException {
        validateFile(file);
        return saveAndResizeAvatar(file, userId);
    }

    /**
     * Save a generated PDF report file.
     * @param fileName file name to use for the PDF
     * @param content PDF bytes
     * @return relative path to saved file
     */
    public String savePdfFile(String fileName, byte[] content) throws IOException {
        Path dirPath = Paths.get(baseStoragePath, "reports");
        Files.createDirectories(dirPath);

        String safeFileName = Paths.get(fileName).getFileName().toString();
        if (!safeFileName.toLowerCase().endsWith(".pdf")) {
            safeFileName = safeFileName + ".pdf";
        }

        Path filePath = dirPath.resolve(safeFileName);
        Files.write(filePath, content);

        log.info("PDF file saved: {}/reports/{}", baseStoragePath, safeFileName);
        return String.join("/", "reports", safeFileName);
    }
    
    /**
     * Resize user avatar to standard dimensions (150x150px)
     * @param file MultipartFile to resize
     * @param userId User ID for file organization
     * @return relative path to saved file
     */
    private String saveAndResizeAvatar(MultipartFile file, String userId) throws IOException {
        // Create directory structure
        Path dirPath = Paths.get(baseStoragePath, "avatars", userId);
        Files.createDirectories(dirPath);
        
        // Generate unique filename
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String fileName = UUID.randomUUID() + extension;
        Path filePath = dirPath.resolve(fileName);
        
        // Save and resize avatar to 150x150 pixels
        try {
            Thumbnails.of(file.getInputStream())
                .size(150, 150)
                .keepAspectRatio(true)
                .outputFormat(getImageFormat(extension))
                .toFile(filePath.toFile());
            
            log.info("Avatar saved and resized: {}/avatars/{}/{}", baseStoragePath, userId, fileName);
        } catch (IOException e) {
            log.error("Failed to resize avatar for user {}", userId, e);
            // Fallback: save original file without resizing
            Files.write(filePath, file.getBytes());
        }
        
        // Return relative path
        return String.join("/", "avatars", userId, fileName);
    }
    
    /**
     * Get image format from file extension
     * @param extension File extension (e.g., ".jpg", ".png")
     * @return Image format for Thumbnailator (e.g., "jpg", "png")
     */
    private String getImageFormat(String extension) {
        if (extension == null) {
            return "jpg";
        }
        String ext = extension.toLowerCase().replace(".", "");
        if ("jpeg".equals(ext)) {
            return "jpg";
        }
        return ext.isEmpty() ? "jpg" : ext;
    }

    /**
     * Load file from storage
     * @param filePath relative path to file
     * @return file bytes
     */
    public byte[] loadFile(String filePath) throws IOException {
        Path path = Paths.get(baseStoragePath, filePath);
        if (!Files.exists(path)) {
            throw new IOException("File not found: " + filePath);
        }
        return Files.readAllBytes(path);
    }

    /**
     * Delete file from storage
     * @param filePath relative path to file
     */
    public void deleteFile(String filePath) {
        try {
            Path path = Paths.get(baseStoragePath, filePath);
            Files.deleteIfExists(path);
            log.info("File deleted: {}", filePath);
        } catch (IOException e) {
            log.warn("Failed to delete file: {}", filePath, e);
        }
    }

    /**
     * Validate file size and MIME type
     * @param file MultipartFile to validate
     * @throws IllegalArgumentException if validation fails
     */
    private void validateFile(MultipartFile file) {
        if (file.isEmpty()) {
            throw new IllegalArgumentException("File is empty");
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds 5MB limit");
        }

        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_IMAGE_TYPES.contains(contentType)) {
            throw new IllegalArgumentException("Invalid file type. Allowed: JPEG, PNG, WebP");
        }
    }

    /**
     * Save file to appropriate subdirectory
     */
    private String saveFile(MultipartFile file, String directory, String entityId) throws IOException {
        // Create directory structure
        Path dirPath = Paths.get(baseStoragePath, directory, entityId);
        Files.createDirectories(dirPath);

        // Generate unique filename with original extension
        String originalFilename = file.getOriginalFilename();
        String extension = originalFilename != null && originalFilename.contains(".") 
            ? originalFilename.substring(originalFilename.lastIndexOf("."))
            : ".jpg";
        String fileName = UUID.randomUUID() + extension;

        // Save file
        Path filePath = dirPath.resolve(fileName);
        Files.write(filePath, file.getBytes());

        log.info("File saved: {}/{}/{}/{}", baseStoragePath, directory, entityId, fileName);

        // Return relative path
        return String.join("/", directory, entityId, fileName);
    }

    /**
     * Get file MIME type
     * @param contentType original content type
     * @return MIME type string
     */
    public String getContentType(String contentType) {
        if (contentType == null || contentType.isEmpty()) {
            return "image/jpeg";
        }
        return contentType;
    }

}
