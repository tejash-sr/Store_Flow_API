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

    @Override
    public void initialize(ExistsInDatabase constraintAnnotation) {
        this.repositoryClass = constraintAnnotation.repositoryClass();
    }

    @Override
    public boolean isValid(Object value, ConstraintValidatorContext context) {
        // Null values are valid (use @NotNull for mandatory validation)
        if (value == null) {
            return true;
        }

        try {
            // Get the repository bean from application context
            Object repositoryBean = applicationContext.getBean(repositoryClass);
            
            if (!(repositoryBean instanceof JpaRepository)) {
                return true;
            }

            JpaRepository repository = (JpaRepository) repositoryBean;
            
            // Check if entity with this ID exists
            if (value instanceof Long) {
                return repository.existsById(value);
            } else if (value instanceof Integer) {
                return repository.existsById(((Integer) value).longValue());
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
