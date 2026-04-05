package com.storeflow.storeflow_api.validation;

import com.storeflow.storeflow_api.config.TestMailConfig;
import com.storeflow.storeflow_api.entity.Category;
import com.storeflow.storeflow_api.entity.Product;
import com.storeflow.storeflow_api.repository.CategoryRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import jakarta.validation.ConstraintValidatorContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests for ExistsInDatabaseValidator custom validator
 * Per PDF spec: Foreign key validations must pass for valid IDs, fail for non-existent
 */
@DataJpaTest
@ActiveProfiles("test")
@Import(TestMailConfig.class)
class ExistsInDatabaseValidatorTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    private ExistsInDatabaseValidator validator;
    private ConstraintValidatorContext context;

    @BeforeEach
    void setUp() {
        validator = new ExistsInDatabaseValidator();
        validator.entityManager = entityManager.getEntityManager();
        context = mock(ConstraintValidatorContext.class);
    }

    /**
     * Test that valid product ID passes validation
     */
    @Test
    void testValidProductId_PassesValidation() {
        // Arrange
        Category category = categoryRepository.save(Category.builder()
                .name("Electronics")
                .isActive(true)
                .build());

        Product product = productRepository.save(Product.builder()
                .name("Test Product")
                .sku("TEST001")
                .price(BigDecimal.valueOf(99.99))
                .category(category)
                .isActive(true)
                .build());

        // Act
        validator.entityMarker = "com.storeflow.storeflow_api.entity.Product";
        validator.idFieldName = "id";
        boolean result = validator.isValid(product.getId(), context);

        // Assert
        assertThat(result).isTrue()
                .as("Validation should pass for existing product ID");
    }

    /**
     * Test that non-existent product ID fails validation
     */
    @Test
    void testNonExistentProductId_FailsValidation() {
        // Act
        validator.entityMarker = "com.storeflow.storeflow_api.entity.Product";
        validator.idFieldName = "id";
        boolean result = validator.isValid(99999L, context);

        // Assert
        assertThat(result).isFalse()
                .as("Validation should fail for non-existent product ID");
    }

    /**
     * Test that null ID passes validation (nullable foreign keys)
     */
    @Test
    void testNullId_PassesValidation() {
        // Act
        validator.entityMarker = "com.storeflow.storeflow_api.entity.Product";
        validator.idFieldName = "id";
        boolean result = validator.isValid(null, context);

        // Assert
        assertThat(result).isTrue()
                .as("Validation should pass for null ID (optional FK)");
    }

    /**
     * Test that valid category ID passes validation
     */
    @Test
    void testValidCategoryId_PassesValidation() {
        // Arrange
        Category category = categoryRepository.save(Category.builder()
                .name("Books")
                .isActive(true)
                .build());

        // Act
        validator.entityMarker = "com.storeflow.storeflow_api.entity.Category";
        validator.idFieldName = "id";
        boolean result = validator.isValid(category.getId(), context);

        // Assert
        assertThat(result).isTrue()
                .as("Validation should pass for existing category ID");
    }

    /**
     * Test that non-existent category ID fails validation
     */
    @Test
    void testNonExistentCategoryId_FailsValidation() {
        // Act
        validator.entityMarker = "com.storeflow.storeflow_api.entity.Category";
        validator.idFieldName = "id";
        boolean result = validator.isValid(99999L, context);

        // Assert
        assertThat(result).isFalse()
                .as("Validation should fail for non-existent category ID");
    }

    /**
     * Test that validator is properly initialized
     */
    @Test
    void testValidatorInitialization() {
        // Arrange
        var annotation = mock(ExistsInDatabase.class);
        when(annotation.entity()).thenReturn(Product.class);
        when(annotation.fieldName()).thenReturn("id");

        // Act
        validator.initialize(annotation);

        // Assert
        assertThat(validator.entityMarker).isNotNull()
                .as("Validator should be initialized with entity marker");
    }

    /**
     * Test that validator can handle string IDs (UUID)
     */
    @Test
    void testStringId_Validation() {
        // This tests UUID-type IDs that some entities might use
        validator.entityMarker = "com.storeflow.storeflow_api.entity.User";
        validator.idFieldName = "id";
        
        // For string IDs, any string would be accepted or rejected based on existence
        // This test just verifies the validator handles the call without error
        boolean result = validator.isValid("nonexistent-uuid-string", context);
        
        assertThat(result).isFalse()
                .as("Non-existent UUID string should fail validation");
    }
}
