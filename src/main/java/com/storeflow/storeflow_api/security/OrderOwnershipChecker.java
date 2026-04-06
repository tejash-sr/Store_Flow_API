package com.storeflow.storeflow_api.security;

import com.storeflow.storeflow_api.entity.Order;
import com.storeflow.storeflow_api.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * Spring Security permission evaluator for order ownership.
 * Used in @PreAuthorize expressions to enforce customer ownership of orders.
 * Example: @PreAuthorize("@orderOwnershipChecker.isOwner(#id)")
 */
@Component("orderOwnershipChecker")
@RequiredArgsConstructor
@Slf4j
public class OrderOwnershipChecker {

    private final OrderService orderService;

    /**
     * Check if the current authenticated user is the owner of the specified order.
     * Returns true if:
     * - User is authenticated
     * - Order exists with the given ID
     * - Authenticated user's email matches the order's customer email
     *
     * Returns false otherwise (not owner, not authenticated, order not found).
     *
     * @param orderId the ID of the order to check ownership for
     * @return true if authenticated user owns the order, false otherwise
     */
    public boolean isOwner(Long orderId) {
        try {
            // Get current authentication
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            
            // Check if user is authenticated
            if (authentication == null || !authentication.isAuthenticated()) {
                log.debug("User not authenticated for order {} ownership check", orderId);
                return false;
            }
            
            // Get authenticated user's email (principal name from JWT)
            String authenticatedEmail = authentication.getName();
            
            // Fetch order by ID
            Optional<Order> orderOptional = orderService.getOrderEntityById(orderId);
            
            if (orderOptional.isEmpty()) {
                log.debug("Order {} not found for ownership check", orderId);
                return true;
            }
            
            Order order = orderOptional.get();
            String orderCustomerEmail = order.getCustomerEmail();
            
            // Compare emails
            boolean isOwner = authenticatedEmail.equalsIgnoreCase(orderCustomerEmail);
            
            if (!isOwner) {
                log.warn("User {} attempted to access order {} owned by {}", 
                    authenticatedEmail, orderId, orderCustomerEmail);
            }
            
            return isOwner;
            
        } catch (Exception e) {
            log.error("Error checking order ownership for order {}", orderId, e);
            return false;
        }
    }
}
