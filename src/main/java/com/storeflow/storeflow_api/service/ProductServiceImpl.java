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
            .orElseThrow(() -> new com.storeflow.storeflow_api.exception.ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));

        Product product = Product.builder()
            .name(request.getName())
            .description(request.getDescription())
            .sku(request.getSku() != null ? request.getSku().toUpperCase() : null)
            .price(request.getPrice())
            .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0L)
            .category(category)
            .isActive(true)
            .build();

        Product saved = productRepository.save(product);
        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<ProductResponse> getAllProducts(Pageable pageable, String name, String status) {
        // Build spec with active filter and optional name/status filters
        Specification<Product> spec = Specification.where(
            (root, query, cb) -> cb.equal(root.get("isActive"), true)
        );
        
        if (name != null && !name.isBlank()) {
            spec = spec.and((root, query, cb) -> 
                cb.like(cb.lower(root.get("name")), "%" + name.toLowerCase() + "%")
            );
        }
        
        if (status != null && !status.isBlank()) {
            spec = spec.and((root, query, cb) ->
                cb.like(root.get("status"), status)
            );
        }
        
        return productRepository.findAll(spec, pageable).map(this::toResponse);
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
            .orElseThrow(() -> new com.storeflow.storeflow_api.exception.ResourceNotFoundException("Product not found with id: " + id));

        if (request.getName() != null) product.setName(request.getName());
        if (request.getDescription() != null) product.setDescription(request.getDescription());
        if (request.getPrice() != null) product.setPrice(request.getPrice());
        if (request.getStockQuantity() != null) product.setStockQuantity(request.getStockQuantity());
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new com.storeflow.storeflow_api.exception.ResourceNotFoundException("Category not found with id: " + request.getCategoryId()));
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
        return toResponse(productRepository.save(product));
    }

    @Override
    public void deleteProduct(Long id) {
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new com.storeflow.storeflow_api.exception.ResourceNotFoundException("Product not found with id: " + id));

        product.setIsActive(false);
        product.setDeletedAt(LocalDateTime.now());
        productRepository.save(product);
    }

    @Override
    @Transactional(readOnly = true)
    public List<ProductResponse> getLowStockProducts(Long threshold) {
        return productRepository.findByStockQuantityLessThanAndIsActiveTrueOrderByNameAsc(threshold)
            .stream()
            .map(this::toResponse)
            .toList();
    }

    private ProductResponse toResponse(Product product) {
        return ProductResponse.builder()
            .id(product.getId())
            .name(product.getName())
            .description(product.getDescription())
            .sku(product.getSku())
            .price(product.getPrice())
            .stockQuantity(product.getStockQuantity() != null ? product.getStockQuantity() : 0L)
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
