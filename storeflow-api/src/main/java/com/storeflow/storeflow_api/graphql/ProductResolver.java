package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.dto.PageResponse;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.service.CategoryService;
import com.storeflow.storeflow_api.service.ProductService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SchemaMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Product Query Resolver
 * Handles product queries and nested field resolution
 */
@Slf4j
@Controller
public class ProductResolver {
    
    private final ProductService productService;
    private final CategoryService categoryService;
    
    public ProductResolver(ProductService productService, CategoryService categoryService) {
        this.productService = productService;
        this.categoryService = categoryService;
    }
    
    /**
     * Query: products - Get paginated list of products with filters
     */
    @QueryMapping
    public Map<String, Object> products(
            @Argument(name = "page") Integer page,
            @Argument(name = "size") Integer size,
            @Argument(name = "sort") String sort,
            @Argument(name = "name") String name,
            @Argument(name = "categoryId") Long categoryId,
            @Argument(name = "status") String status,
            @Argument(name = "minPrice") Double minPrice,
            @Argument(name = "maxPrice") Double maxPrice
    ) {
        page = page != null ? page : 0;
        size = size != null && size <= 100 ? size : 20;
        sort = sort != null ? sort : "createdAt,desc";
        
        // Parse sort string
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equals(sortParts[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = sortParts[0];
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        
        // TODO: Implement filtered search
        Page<Product> productPage = productService.getAllProducts(pageRequest);
        
        return buildProductPageResponse(productPage);
    }
    
    /**
     * Query: product - Get single product by ID
     */
    @QueryMapping
    public Product product(@Argument Long id) {
        return productService.getProductById(id);
    }
    
    /**
     * SchemaMapping: category - Resolve category field for Product type
     */
    @SchemaMapping(typeName = "Product")
    public Category category(Product product) {
        if (product.getCategory() != null) {
            return product.getCategory();
        }
        if (product.getCategoryId() != null) {
            return categoryService.getCategoryById(product.getCategoryId());
        }
        return null;
    }
    
    /**
     * Build product page response with pagination info
     */
    private Map<String, Object> buildProductPageResponse(Page<Product> page) {
        Map<String, Object> response = new HashMap<>();
        response.put("content", page.getContent());
        
        Map<String, Object> pageInfo = new HashMap<>();
        pageInfo.put("page", page.getNumber());
        pageInfo.put("size", page.getSize());
        pageInfo.put("totalElements", page.getTotalElements());
        pageInfo.put("totalPages", page.getTotalPages());
        pageInfo.put("first", page.isFirst());
        pageInfo.put("last", page.isLast());
        pageInfo.put("hasNext", page.hasNext());
        
        response.put("pageInfo", pageInfo);
        return response;
    }
}
