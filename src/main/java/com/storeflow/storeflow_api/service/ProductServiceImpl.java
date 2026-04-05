package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.ProductRequest;
import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.entity.enums.ProductStatus;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Implementation of ProductService with business logic.
 */
@Service
@RequiredArgsConstructor
@Transactional
public class ProductServiceImpl implements ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    @Override
    public ProductResponse createProduct(ProductRequest request) {
        Category category = categoryRepository.findById(request.getCategoryId())
            .orElseThrow(() -> new IllegalArgumentException("Category not found"));

        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .sku(request.getSku() != null ? request.getSku().toUpperCase() : null)
            .price(request.getPrice())
            .category(category)
            .isActive(true)
            .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable, String name, String status) {
        List<Product> products = productRepository.findByIsActiveTrueOrderByNameAsc();
        
        // Client-side filtering for MVP (server-side in Phase 4)
        List<ProductResponse> responses = products.stream()
            .map(this::toResponse)
            .toList();

        // Simple pagination
        int start = (int) pageable.getOffset();
        int end = Math.min(start + pageable.getPageSize(), responses.size());
        List<ProductResponse> pageContent = responses.subList(start, end);

        return new PageImpl<>(pageContent, pageable, responses.size());
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> searchProducts(Specification<Product> spec, Pageable pageable) {
        Page<Product> products = productRepository.findAll(spec, pageable);
        return products.map(this::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Optional<ProductResponse> getProductById(Long id) {
        return productRepository.findById(id)
            .filter(Product::getIsActive)
            .map(this::toResponse);
    }

    @Override
    public ProductResponse updateProduct(Long id, ProductRequest request) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new IllegalArgumentException("Category not found"));
            product.setCategory(category);
        }

        Product updated = productRepository.save(product);
        return toResponse(updated);
    }

    @Override
    public ProductResponse adjustStock(Long id, Long quantityChange) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new com.storeflow.storeflow_api.exception.ResourceNotFoundException("Product not found with id: " + id));

        long newQty = product.getStockQuantity() + quantityChange;
        if (newQty < 0) {
            throw new com.storeflow.storeflow_api.exception.InsufficientStockException("Stock cannot go below zero. Available: " + product.getStockQuantity());
        }
        product.setStockQuantity(newQty);

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new IllegalArgumentException("Product not found"));

        product.setIsActive(false);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .price(product.getPrice())
            .stockQuantity(0L) // TODO: Get from InventoryItem in Phase 4
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : "Uncategorized")
            .categoryId(product.getCategory() != null ? product.getCategory().getId() : null)
            .category(product.getCategory())
            .imageUrl(null) // TODO: Add imageUrl field to Product in Phase 4
            .status(product.getIsActive() ? ProductStatus.ACTIVE : ProductStatus.DISCONTINUED)
            .createdAt(product.getCreatedAt())
            .updatedAt(product.getUpdatedAt())
            .build();
    }
}
