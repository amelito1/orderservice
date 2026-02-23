package it.euris.orderservices.service;

import feign.FeignException;
import it.euris.orderservices.components.OrderStateFactory;
import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.dto.interfaces.ProductProxy;
import it.euris.orderservices.dto.request.OrderRequest;
import it.euris.orderservices.dto.request.OrderedProduct;
import it.euris.orderservices.dto.response.OrderChangeStateResponse;
import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import it.euris.orderservices.entities.OrderEntity;
import it.euris.orderservices.entities.OrderedProductEntity;
import it.euris.orderservices.repositories.OrderRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class OrderServiceTest {

    @Mock
    private OrderRepository  orderRepositoryMock;

    @Mock
    private OrderStateFactory  orderStateFactoryMock;

    @Mock
    private ProductProxy  productProxyMock;

    @Mock
    private OrderState orderStateMock;

    @InjectMocks
    private OrderService orderService;

    private List<ProductOrderedResponse> orderedProduct() {

           final ProductOrderedResponse mockedOrderedProduct = new ProductOrderedResponse(
                1L,
                "test",
                5.0,
                BigDecimal.valueOf(34)
        );
        return List.of(mockedOrderedProduct);
    }



    @Test
   void createOrderSuccess() {
        OrderRequest  orderRequest = new OrderRequest();
        OrderedProduct  orderedProduct = new OrderedProduct();

        orderedProduct.setProductQuantity(5.0);
        orderedProduct.setProductId(1L);
        orderedProduct.setProductUnitPrice(BigDecimal.valueOf(23));

        List<OrderedProduct> products =  List.of(orderedProduct);
        orderRequest.setCustomerId(1L);

        orderRequest.setOrderedProducts(products);

        final OrderEntity orderEntityResponse = getOrderEntity();


        when(productProxyMock.retrievesOrderedProducts(products)).thenReturn(this.orderedProduct());

        OrderState order = Mockito.mock(OrderState.class);

        when(orderStateFactoryMock.getState(OrderStatus.ORDERED)).thenReturn(order);

        when(orderRepositoryMock.save(any(OrderEntity.class))).thenReturn(orderEntityResponse);

        final OrderResponse result = this.orderService.createOrder(orderRequest);

        verify(productProxyMock).retrievesOrderedProducts(products);

        verify(orderRepositoryMock).save(any(OrderEntity.class));

        assertNotNull(result);

        assertEquals(BigDecimal.valueOf(170), result.totalPrice());
    }

    private static OrderEntity getOrderEntity() {
        OrderStatus status = OrderStatus.ORDERED;


        final OrderEntity orderEntityResponse = new OrderEntity();

        final OrderedProductEntity  orderedProductEntityMocked = new OrderedProductEntity();

        orderedProductEntityMocked.setOrder(orderEntityResponse);

        orderedProductEntityMocked.setId(1L);

        orderedProductEntityMocked.setProductName("test");

        orderedProductEntityMocked.setProductId(1L);

        orderedProductEntityMocked.setId(1L);
        orderedProductEntityMocked.setUnitPrice(BigDecimal.valueOf(23));

        orderEntityResponse.setId(1L);
        orderEntityResponse.setCustomerId(1L);
        orderEntityResponse.setOrderStatus(status);
        orderEntityResponse.setOrderedProduct(List.of(orderedProductEntityMocked));
        orderEntityResponse.setTotalPrice(BigDecimal.valueOf(170));
        return orderEntityResponse;
    }

    @Test
    void create_whenFeignClientFails_shouldThrow() {

        doThrow(new RuntimeException("Feign client unavailable"))
                .when(productProxyMock)
                .retrievesOrderedProducts(any());


        assertThrows(RuntimeException.class,
                () -> orderService.createOrder(new OrderRequest() ));

    }


    private OrderRequest getOrderRequest() {
        final OrderRequest orderRequest = new OrderRequest();

        orderRequest.setCustomerId(1L);
        final OrderedProduct product = new OrderedProduct();

        product.setProductQuantity(10.0);
        product.setProductId(1L);

        product.setProductUnitPrice(BigDecimal.valueOf(23));

        orderRequest.setOrderedProducts(List.of(product));
        return orderRequest;
    }

    private List<OrderedProduct> getOrderedProducts(){
        final OrderedProduct orderedProduct  = new OrderedProduct();
        orderedProduct.setProductId(1L);
        orderedProduct.setProductQuantity(5.0);
        orderedProduct.setProductUnitPrice(BigDecimal.valueOf(23));
        return  List.of(orderedProduct);
    }


    @Test
    void createOrder_insufficientStock_shouldThrow() {
        OrderedProduct requested = new OrderedProduct();
        requested.setProductId(1L);
        requested.setProductQuantity(5.0);

        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setCustomerId(1L);
        orderRequest.setOrderedProducts(List.of(requested));

        doThrow(new RuntimeException("Insufficient stock"))
                .when(productProxyMock)
                .retrievesOrderedProducts(any());

        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> orderService.createOrder(orderRequest));

        assertEquals("Insufficient stock", ex.getMessage());

        verifyNoInteractions(this.orderStateFactoryMock);
    }

    @Test
    void getAllOrders_whenOrders_shouldReturnListOfOrders() {
        OrderEntity orderEntity = new OrderEntity();

        orderEntity.setId(1L);
        orderEntity.setCustomerId(1L);

        orderEntity.setTotalPrice(BigDecimal.valueOf(35000));

        orderEntity.setOrderStatus(OrderStatus.ORDERED);

        OrderedProductEntity orderedProductEntity = new OrderedProductEntity();
        orderedProductEntity.setId(1L);
        orderedProductEntity.setUnitPrice(BigDecimal.valueOf(23));
        orderedProductEntity.setProductName("test");
        orderedProductEntity.setProductId(1L);
        orderedProductEntity.setId(1L);
        orderedProductEntity.setOrder(orderEntity);
        orderedProductEntity.setQuantity(9);

        orderEntity.setOrderedProduct(List.of(orderedProductEntity));

        when(orderRepositoryMock.findAll()).thenReturn(List.of(orderEntity));

        var result = this.orderService.retrieveOrders();

        assertNotNull(result);

        verify(orderRepositoryMock).findAll();
    }

    @Test
    void getAllOrders_whenNoOrders_shouldReturnEmptyList() {
        when(orderRepositoryMock.findAll()).thenReturn(List.of());
        var result = this.orderService.retrieveOrders();
        assertEquals(0, result.size());
    }

    @Test
    void retrievesOrderPages_success() {

        int page = 0;
        int size = 2;

        OrderedProductEntity productEntity = new OrderedProductEntity();
        productEntity.setProductId(1L);
        productEntity.setProductName("Laptop");
        productEntity.setQuantity(2);
        productEntity.setUnitPrice(BigDecimal.valueOf(999.99));

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(1L);
        orderEntity.setCustomerId(10L);
        orderEntity.setTotalPrice(BigDecimal.valueOf(1999.98));
        orderEntity.setOrderStatus(OrderStatus.ORDERED);
        orderEntity.setOrderedProduct(List.of(productEntity));

        Page<OrderEntity> fakePage = new PageImpl<>(
                List.of(orderEntity),
                PageRequest.of(page, size),
                1
        );

        when(orderRepositoryMock.findAll(PageRequest.of(page, size)))
                .thenReturn(fakePage);

        Page<OrderResponse> result = orderService.retrievesOrderPages(page, size);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(1, result.getContent().size());

        OrderResponse response = result.getContent().getFirst();
        assertEquals(1L, response.orderId());
        assertEquals(10L, response.customer());

        verify(orderRepositoryMock).findAll(PageRequest.of(page, size));
    }

    @Test
    void retrievesOrderPages_emptyPage() {

        Page<OrderEntity> emptyPage = new PageImpl<>(
                List.of(),
                PageRequest.of(0, 5),
                0
        );

        when(orderRepositoryMock.findAll(any(Pageable.class)))
                .thenReturn(emptyPage);


        Page<OrderResponse> result = orderService.retrievesOrderPages(0, 5);


        assertNotNull(result);
        assertTrue(result.isEmpty());
        assertEquals(0, result.getTotalElements());

        verify(orderRepositoryMock).findAll(any(Pageable.class));
    }

    @Test
    void orderDelivered_success() {
        Long orderId = 1L;

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setOrderStatus(OrderStatus.ORDERED);

        when(orderRepositoryMock.findById(orderId))
                .thenReturn(Optional.of(orderEntity));

        when(orderStateFactoryMock.getState(OrderStatus.ORDERED))
                .thenReturn(orderStateMock);

        doAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setOrderStatus(OrderStatus.DELIVERED);
            return null;
        }).when(orderStateMock).delivered(orderEntity);


        OrderChangeStateResponse response = orderService.orderDelivered(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        assertEquals(OrderStatus.DELIVERED, response.orderStatus());

        verify(orderRepositoryMock).findById(orderId);
        verify(orderStateFactoryMock).getState(OrderStatus.ORDERED);
        verify(orderStateMock).delivered(orderEntity);
    }

    @Test
    void orderDelivered_orderNotFound_shouldThrow() {
        Long orderId = 99L;
        when(orderRepositoryMock.findById(orderId))
                .thenReturn(Optional.empty());

        assertThrows(RuntimeException.class,
                () -> orderService.orderDelivered(orderId));
        verifyNoInteractions(orderStateFactoryMock);
    }

    @Test
    void orderDelivered_invalidState_shouldThrow() {
        Long orderId = 1L;
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setOrderStatus(OrderStatus.DELIVERED);

        when(orderRepositoryMock.findById(orderId))
                .thenReturn(Optional.of(orderEntity));

        when(orderStateFactoryMock.getState(OrderStatus.DELIVERED))
                .thenReturn(orderStateMock);

        doThrow(new IllegalStateException("Order already delivered"))
                .when(orderStateMock)
                .delivered(any());

        assertThrows(IllegalStateException.class,
                () -> orderService.orderDelivered(orderId));
    }

    @Test
    void orderCanceled_success() {
        Long orderId = 2L;

        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId(orderId);
        orderEntity.setOrderStatus(OrderStatus.CANCELED);

        when(orderRepositoryMock.findById(orderId))
                .thenReturn(Optional.of(orderEntity));

        when(orderStateFactoryMock.getState(OrderStatus.CANCELED))
                .thenReturn(orderStateMock);

        doAnswer(invocation -> {
            OrderEntity order = invocation.getArgument(0);
            order.setOrderStatus(OrderStatus.CANCELED);
            return null;
        }).when(orderStateMock).delivered(orderEntity);


        OrderChangeStateResponse response = orderService.orderDelivered(orderId);

        assertNotNull(response);
        assertEquals(orderId, response.orderId());
        assertEquals(OrderStatus.CANCELED, response.orderStatus());

        verify(orderRepositoryMock).findById(orderId);
        verify(orderStateFactoryMock).getState(OrderStatus.CANCELED);
        verify(orderStateMock).delivered(orderEntity);
    }
}
