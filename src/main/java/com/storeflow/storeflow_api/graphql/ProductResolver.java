package com.storeflow.storeflow_api.graphql;

import com.storeflow.storeflow_api.dto.ProductResponse;
import com.storeflow.storeflow_api.service.ProductService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.stereotype.Controller;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * GraphQL Query Resolver
 * Handles product queries and nested field resolution
 * REST and GraphQL APIs work simultaneously
 */
@Slf4j
@Controller
@RequiredArgsConstructor
public class ProductResolver {
    
    private final ProductService productService;
    
    /**
     * Query: products - Get paginated list of products with filters
     * @return ProductPage with content and pagination info
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
        
        log.info("GraphQL: Fetching products - page: {}, size: {}, filters: name={}, status={}", page, size, name, status);
        
        // Parse sort string
        String[] sortParts = sort.split(",");
        Sort.Direction direction = sortParts.length > 1 && "asc".equalsIgnoreCase(sortParts[1]) 
                ? Sort.Direction.ASC : Sort.Direction.DESC;
        String sortBy = sortParts[0];
        
        PageRequest pageRequest = PageRequest.of(page, size, Sort.by(direction, sortBy));
        Page<ProductResponse> productPage = productService.getAllProducts(pageRequest, name, status);
        
        return buildProductPageResponse(productPage);
    }
    
    /**
     * Query: product - Get single product by ID
     */
    @QueryMapping
    public ProductResponse product(@Argument Long id) {
        log.info("GraphQL: Fetching product with id: {}", id);
        return productService.getProductById(id).orElse(null);
    }
    
    /**
     * Build product page response with pagination info
     */
    private Map<String, Object> buildProductPageResponse(Page<ProductResponse> page) {
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
