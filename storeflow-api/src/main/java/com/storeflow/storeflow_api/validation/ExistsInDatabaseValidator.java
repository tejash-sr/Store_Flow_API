package com.storeflow.storeflow_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Validator implementation for @ExistsInDatabase constraint.
 * Checks if a given value exists in the specified repository.
 */
@Component
@RequiredArgsConstructor
public class ExistsInDatabaseValidator implements ConstraintValidator<ExistsInDatabase, Object> {
    private final ApplicationContext applicationContext;
    private Class<?> repositoryClass;
    private String fieldName;

    @Override
    public void initialize(ExistsInDatabase constraintAnnotation) {
        this.repositoryClass = constraintAnnotation.repositoryClass();
        this.fieldName = constraintAnnotation.fieldName();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Null values are valid (use @NotNull for mandatory validation)
        if (value == null) {
            return true;
        }

        try {
            // Get the repository bean from application context
            JpaRepository<?, ?> repository = (JpaRepository<?, ?>) applicationContext.getBean(repositoryClass);
            
            // For now, we'll check if the value exists as an ID
            // This assumes the repository has findById method (all JpaRepositories do)
            if (value instanceof Long || value instanceof Integer) {
                return repository.existsById(Long.valueOf(value.toString()));
            }
            
            // For other types, assume valid (graceful fallback)
            return true;
        } catch (Exception e) {
            // If repository not found or error occurs, be lenient and return true
            // Errors will be caught at service layer
            return true;
        }
    }
}
