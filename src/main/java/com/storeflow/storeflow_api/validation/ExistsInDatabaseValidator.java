package com.storeflow.storeflow_api.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Component;

/**
 * Validator implementation for @ExistsInDatabase constraint.
 * Checks if a given value exists in the specified repository.
 */
@Component
@RequiredArgsConstructor
@Slf4j
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
                // If it's not a JpaRepository, assume valid
                return true;
            }

            JpaRepository repository = (JpaRepository) repositoryBean;
            
            // Check if entity with this ID exists
            if (value instanceof Long) {
                boolean exists = repository.existsById(value);
                if (!exists && context != null) {
                    // Add helpful error message
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Entity with this ID does not exist")
                        .addConstraintViolation();
                }
                return exists;
            } else if (value instanceof Integer) {
                boolean exists = repository.existsById(((Integer) value).longValue());
                if (!exists && context != null) {
                    context.disableDefaultConstraintViolation();
                    context.buildConstraintViolationWithTemplate("Entity with this ID does not exist")
                        .addConstraintViolation();
                }
                return exists;
            }
            
            // For other types, assume valid (graceful fallback)
            return true;
        } catch (Exception e) {
            // If repository not found or error occurs, be lenient and return true
            // Errors will be caught at service layer
            log.warn("Validation error in @ExistsInDatabase: {}, allowing validation to pass", e.getMessage());
            return true;
        }
    }
}
