package com.storeflow.storeflow_api.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;
import java.lang.annotation.*;

/**
 * Custom constraint annotation to validate that a value exists in the database.
 * Used for foreign key validation (e.g., categoryId must reference an existing category).
 * 
 * Example usage:
 *   @ExistsInDatabase(repositoryClass = CategoryRepository.class, 
 *                      fieldName = "id")
 *   private Long categoryId;
 */
@Target({ElementType.FIELD, ElementType.PARAMETER})
@Retention(RetentionPolicy.RUNTIME)
@Constraint(validatedBy = ExistsInDatabaseValidator.class)
@Documented
public @interface ExistsInDatabase {
    String message() default "{com.storeflow.validation.ExistsInDatabase.message}";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};

    /**
     * The repository class to use for existence check
     */
    Class<?> repositoryClass();

    /**
     * The field name to search for (usually "id", but can be other unique fields)
     */
    String fieldName() default "id";
}
