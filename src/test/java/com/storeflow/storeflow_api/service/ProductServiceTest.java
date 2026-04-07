package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.exception.ResourceNotFoundException;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductService using Mockito mocks.
 * Tests business logic without database dependencies.
 */
@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Category testCategory;
    private Product testProduct;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
            .id(1L)
            .name("Electronics")
            .build();

        testProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .sku("PROD-001")
            .description("Test Description")
            .price(BigDecimal.valueOf(99.99))
            .stockQuantity(100L)
            .category(testCategory)
            .isActive(true)
            .build();
    }

    @Test
    void createProduct_WithValidCategory_ReturnsProductResponse() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
            .name("New Product")
            .sku("NEW-001")
            .description("New Description")
            .price(BigDecimal.valueOf(49.99))
            .stockQuantity(50L)
            .categoryId(1L)
            .build();

        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductResponse response = productService.createProduct(request);

        // Assert
        assertNotNull(response);
        assertEquals("Test Product", response.getName());
        assertEquals("PROD-001", response.getSku());
        verify(categoryRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void createProduct_WithInvalidCategory_ThrowsResourceNotFoundException() {
        // Arrange
        ProductRequest request = ProductRequest.builder()
            .name("New Product")
            .sku("NEW-001")
            .description("New Description")
            .price(BigDecimal.valueOf(49.99))
            .stockQuantity(50L)
            .categoryId(999L)
            .build();

        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.createProduct(request);
        });
        verify(categoryRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void deleteProduct_WithValidId_SetsIsActiveFalse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        productService.deleteProduct(1L);

        // Assert
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(argThat(product -> 
            product.getId() == 1L && !product.getIsActive()
        ));
        assertFalse(testProduct.getIsActive());
    }

    @Test
    void deleteProduct_WithInvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.deleteProduct(999L);
        });
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void getProductById_WithValidId_ReturnsProductResponse() {
        // Arrange
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act
        Optional<ProductResponse> response = productService.getProductById(1L);

        // Assert
        assertTrue(response.isPresent());
        assertEquals("Test Product", response.get().getName());
        assertEquals("PROD-001", response.get().getSku());
        verify(productRepository, times(1)).findById(1L);
    }

    @Test
    void getProductById_WithInvalidId_ReturnsEmpty() {
        // Arrange
        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<ProductResponse> response = productService.getProductById(999L);

        // Assert
        assertFalse(response.isPresent());
        verify(productRepository, times(1)).findById(999L);
    }

    @Test
    void getProductById_WithInactiveProduct_ReturnsEmpty() {
        // Arrange
        Product inactiveProduct = Product.builder()
            .id(2L)
            .name("Inactive Product")
            .sku("INACTIVE-001")
            .price(BigDecimal.valueOf(25.00))
            .isActive(false)
            .category(testCategory)
            .build();

        when(productRepository.findById(2L)).thenReturn(Optional.of(inactiveProduct));

        // Act
        Optional<ProductResponse> response = productService.getProductById(2L);

        // Assert
        assertFalse(response.isPresent());
        verify(productRepository, times(1)).findById(2L);
    }

    @Test
    void updateProduct_WithValidData_UpdatesProductSuccessfully() {
        // Arrange
        ProductRequest updateRequest = ProductRequest.builder()
            .name("Updated Product")
            .description("Updated Description")
            .price(BigDecimal.valueOf(149.99))
            .stockQuantity(200L)
            .categoryId(1L)
            .build();

        Product updatedProduct = Product.builder()
            .id(1L)
            .name("Updated Product")
            .sku("PROD-001")
            .description("Updated Description")
            .price(BigDecimal.valueOf(149.99))
            .stockQuantity(200L)
            .category(testCategory)
            .isActive(true)
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductResponse response = productService.updateProduct(1L, updateRequest);

        // Assert
        assertNotNull(response);
        assertEquals("Updated Product", response.getName());
        assertEquals("Updated Description", response.getDescription());
        assertEquals(BigDecimal.valueOf(149.99), response.getPrice());
        assertEquals(200L, response.getStockQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void updateProduct_WithInvalidId_ThrowsResourceNotFoundException() {
        // Arrange
        ProductRequest updateRequest = ProductRequest.builder()
            .name("Updated Product")
            .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(999L, updateRequest);
        });
        verify(productRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void updateProduct_WithInvalidCategory_ThrowsResourceNotFoundException() {
        // Arrange
        ProductRequest updateRequest = ProductRequest.builder()
            .name("Updated Product")
            .categoryId(999L)
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            productService.updateProduct(1L, updateRequest);
        });
        verify(productRepository, times(1)).findById(1L);
        verify(categoryRepository, times(1)).findById(999L);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void adjustStock_WithPositiveChange_IncreasesStock() {
        // Arrange
        testProduct.setStockQuantity(100L);
        Product updatedProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .sku("PROD-001")
            .price(BigDecimal.valueOf(99.99))
            .stockQuantity(150L)
            .category(testCategory)
            .isActive(true)
            .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // Act
        ProductResponse response = productService.adjustStock(1L, 50L);

        // Assert
        assertNotNull(response);
        assertEquals(150L, response.getStockQuantity());
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void adjustStock_WithNegativeChangeExceedingStock_ThrowsInsufficientStockException() {
        // Arrange
        testProduct.setStockQuantity(50L);
        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

        // Act & Assert
        assertThrows(com.storeflow.storeflow_api.exception.InsufficientStockException.class, () -> {
            productService.adjustStock(1L, -100L);
        });
        verify(productRepository, times(1)).findById(1L);
        verify(productRepository, never()).save(any(Product.class));
    }
}
