package com.storeflow.storeflow_api.service;

import com.storeflow.storeflow_api.dto.OrderItemRequest;
import com.storeflow.storeflow_api.dto.OrderRequest;
import com.storeflow.storeflow_api.dto.OrderResponse;
import com.storeflow.storeflow_api.entity.*;
import com.storeflow.storeflow_api.entity.Order.OrderStatus;
import com.storeflow.storeflow_api.exception.InsufficientStockException;
import com.storeflow.storeflow_api.exception.ResourceNotFoundException;
import com.storeflow.storeflow_api.repository.OrderRepository;
import com.storeflow.storeflow_api.repository.ProductRepository;
import com.storeflow.storeflow_api.repository.StoreRepository;
import com.storeflow.storeflow_api.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for OrderService using Mockito mocks.
 * Tests business logic for order placement and status management.
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class OrderServiceTest {

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StoreRepository storeRepository;

    @Mock
    private SimpMessagingTemplate messagingTemplate;

    @Mock
    private EmailService emailService;

    @InjectMocks
    private OrderServiceImpl orderService;

    private Category testCategory;
    private Product testProduct;
    private Store testStore;
    private User testUser;
    private Order testOrder;

    @BeforeEach
    void setUp() {
        // Clear SecurityContext
        SecurityContextHolder.clearContext();

        testCategory = Category.builder()
            .id(1L)
            .name("Electronics")
            .build();

        testProduct = Product.builder()
            .id(1L)
            .name("Test Product")
            .sku("PROD-001")
            .price(new BigDecimal("100.00"))
            .stockQuantity(50L)
            .category(testCategory)
            .isActive(true)
            .build();

        testStore = Store.builder()
            .id(1L)
            .name("Main Store")
            .isActive(true)
            .build();

        testUser = User.builder()
            .id(UUID.randomUUID())
            .email("user@test.com")
            .fullName("Test User")
            .build();

        testOrder = Order.builder()
            .id(1L)
            .orderNumber("ORD-001")
            .store(testStore)
            .customer(testUser)
            .customerName("Test User")
            .customerEmail("user@test.com")
            .status(OrderStatus.PENDING)
            .subtotal(new BigDecimal("100.00"))
            .total(new BigDecimal("100.00"))
            .items(new ArrayList<>())
            .build();

        // Mock messagingTemplate to avoid NullPointerException
        doNothing().when(messagingTemplate).convertAndSend(anyString(), any(Map.class));
        doNothing().when(messagingTemplate).convertAndSendToUser(anyString(), anyString(), any(Map.class));
    }

    @Test
    void placeOrder_WithValidItems_CreatesOrderSuccessfully() {
        // Arrange
        OrderItemRequest itemRequest = OrderItemRequest.builder()
            .productId(1L)
            .quantity(2L)
            .build();

        OrderRequest orderRequest = OrderRequest.builder()
            .items(List.of(itemRequest))
            .build();

        // Mock security context - anonymous user
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(storeRepository.findAll()).thenReturn(List.of(testStore));
        when(orderRepository.save(any(Order.class))).thenReturn(testOrder);

        // Act
        OrderResponse response = orderService.placeOrder(orderRequest);

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.PENDING.name(), response.getStatus());
        // validateOrder calls findById twice (validation + stock deduction), then placeOrder calls once for order items
        verify(productRepository, times(3)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(storeRepository, times(1)).findAll();
    }

    @Test
    void placeOrder_WithMissingProduct_ThrowsResourceNotFoundException() {
        // Arrange
        OrderItemRequest itemRequest = OrderItemRequest.builder()
            .productId(999L)
            .quantity(2L)
            .build();

        OrderRequest orderRequest = OrderRequest.builder()
            .items(List.of(itemRequest))
            .build();

        // Mock security context - anonymous user
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.placeOrder(orderRequest);
        });
        verify(productRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void placeOrder_WithNoStores_ThrowsResourceNotFoundException() {
        // Arrange
        OrderItemRequest itemRequest = OrderItemRequest.builder()
            .productId(1L)
            .quantity(2L)
            .build();

        OrderRequest orderRequest = OrderRequest.builder()
            .items(List.of(itemRequest))
            .build();

        // Mock security context - anonymous user
        SecurityContext securityContext = mock(SecurityContext.class);
        Authentication authentication = mock(Authentication.class);
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(false);
        SecurityContextHolder.setContext(securityContext);

        when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
        when(storeRepository.findAll()).thenReturn(Collections.emptyList());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.placeOrder(orderRequest);
        });
        verify(storeRepository, times(2)).findAll(); // Called twice: findAll() for filtered active, then fallback
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithValidTransition_UpdatesSuccessfully() {
        // Arrange
        testOrder.setStatus(OrderStatus.PENDING);
        Order confirmedOrder = Order.builder()
            .id(1L)
            .orderNumber("ORD-001")
            .store(testStore)
            .customer(testUser)
            .customerName("Test User")
            .customerEmail("user@test.com")
            .status(OrderStatus.CONFIRMED)
            .subtotal(new BigDecimal("100.00"))
            .total(new BigDecimal("100.00"))
            .items(new ArrayList<>())
            .build();

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));
        when(orderRepository.save(any(Order.class))).thenReturn(confirmedOrder);

        // Act
        OrderResponse response = orderService.updateOrderStatus(1L, "CONFIRMED");

        // Assert
        assertNotNull(response);
        assertEquals(OrderStatus.CONFIRMED.name(), response.getStatus());
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, times(1)).save(any(Order.class));
        verify(messagingTemplate, times(1)).convertAndSend(anyString(), any(Map.class));
        verify(messagingTemplate, times(1)).convertAndSendToUser(anyString(), anyString(), any(Map.class));
    }

    @Test
    void updateOrderStatus_WithInvalidTransition_ThrowsIllegalArgumentException() {
        // Arrange
        testOrder.setStatus(OrderStatus.DELIVERED);

        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert - DELIVERED cannot transition to PENDING
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(1L, "PENDING");
        });
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithInvalidStatus_ThrowsIllegalArgumentException() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act & Assert - INVALID_STATUS doesn't exist
        assertThrows(IllegalArgumentException.class, () -> {
            orderService.updateOrderStatus(1L, "INVALID_STATUS");
        });
        verify(orderRepository, times(1)).findById(1L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void updateOrderStatus_WithNonexistentOrder_ThrowsResourceNotFoundException() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> {
            orderService.updateOrderStatus(999L, "CONFIRMED");
        });
        verify(orderRepository, times(1)).findById(999L);
        verify(orderRepository, never()).save(any(Order.class));
    }

    @Test
    void getOrderById_WithValidId_ReturnsOrderResponse() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<OrderResponse> response = orderService.getOrderById(1L);

        // Assert
        assertTrue(response.isPresent());
        assertEquals(OrderStatus.PENDING.name(), response.get().getStatus());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getOrderById_WithInvalidId_ReturnsEmpty() {
        // Arrange
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // Act
        Optional<OrderResponse> response = orderService.getOrderById(999L);

        // Assert
        assertFalse(response.isPresent());
        verify(orderRepository, times(1)).findById(999L);
    }

    @Test
    void getOrderEntityById_WithValidId_ReturnsOrder() {
        // Arrange
        when(orderRepository.findById(1L)).thenReturn(Optional.of(testOrder));

        // Act
        Optional<Order> response = orderService.getOrderEntityById(1L);

        // Assert
        assertTrue(response.isPresent());
        assertEquals("ORD-001", response.get().getOrderNumber());
        verify(orderRepository, times(1)).findById(1L);
    }

    @Test
    void getAllOrdersForExport_ReturnsAllOrders() {
        // Arrange
        Order order2 = Order.builder()
            .id(2L)
            .orderNumber("ORD-002")
            .status(OrderStatus.CONFIRMED)
            .items(new ArrayList<>())
            .build();

        when(orderRepository.findAll()).thenReturn(List.of(testOrder, order2));

        // Act
        List<Order> orders = orderService.getAllOrdersForExport();

        // Assert
        assertNotNull(orders);
        assertEquals(2, orders.size());
        assertEquals("ORD-001", orders.get(0).getOrderNumber());
        assertEquals("ORD-002", orders.get(1).getOrderNumber());
        verify(orderRepository, times(1)).findAll();
    }
}
