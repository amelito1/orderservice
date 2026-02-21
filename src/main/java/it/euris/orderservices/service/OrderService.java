package it.euris.orderservices.service;
import it.euris.common.PageUtils;
import it.euris.orderservices.components.OrderStateFactory;
import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.dto.interfaces.ProductProxy;
import it.euris.orderservices.dto.request.OrderRequest;
import it.euris.orderservices.dto.request.OrderedProduct;
import it.euris.orderservices.dto.request.RestoreProductRequest;
import it.euris.orderservices.dto.response.OrderChangeStateResponse;
import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.dto.response.PartialTotalPrice;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import it.euris.orderservices.entities.OrderEntity;
import it.euris.orderservices.entities.OrderedProductEntity;
import it.euris.orderservices.repositories.OrderRepository;

import it.euris.orderservices.repositories.OrderedProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import java.math.BigDecimal;
import java.util.List;

import static it.euris.orderservices.utilities.OrderUtilities.mapToResponseFromEntity;

@Service
@Transactional
public class OrderService {

    @Autowired
    private final OrderRepository orderRepository;

    @Autowired
    private final OrderStateFactory orderStateFactory;

    @Autowired
    private final ProductProxy productProxy;

    @Autowired
    private final OrderedProductRepository orderedProductRepository;

    public OrderService(
            OrderRepository orderRepository,
            ProductProxy productProxy,
            OrderStateFactory orderStateFactory,
            OrderedProductRepository orderedProductRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderStateFactory = orderStateFactory;
        this.productProxy = productProxy;
        this.orderedProductRepository = orderedProductRepository;
    }


    public OrderResponse createOrder(OrderRequest orderRequest) {

        final List<ProductOrderedResponse> orderedResponses = this.productProxy
                .retrievesOrderedProducts(( orderRequest.getOrderedProducts()));

        final BigDecimal totalPrice = this.calculateTotalPricePerProduct(orderRequest);


        final List<String> productsIds = this.retrieveProductIdsFromRequest(orderRequest);

        final OrderEntity orderEntity = new OrderEntity();


        final OrderState order = this.orderStateFactory.getState(OrderStatus.ORDERED);


        orderEntity.setTotalPrice(totalPrice);
        orderEntity.setCustomerId(orderRequest.getCustomerId());

        orderEntity.setOrderStatus(order.getStatus());

       final List<OrderedProductEntity> productList = orderRequest
                .getOrderedProducts()
                .stream()
                .map(p -> {
                    final OrderedProductEntity orderedProduct = new OrderedProductEntity();

                    orderedProduct.setProductId(p.getProductId());
                    orderedProduct.setQuantity(BigDecimal.valueOf(p.getProductQuantity()));

                    orderedProduct.setOrder(orderEntity);
                    return orderedProduct;
                }).toList();


        orderEntity.setOrderedProduct(productList);

        final OrderEntity savedOrder = this.orderRepository.save(orderEntity);



        return mapToResponseFromEntity(savedOrder, orderedResponses);
    }

    public List<OrderResponse> retrieveOrders() {

        final List<OrderEntity> orderEntities = this.orderRepository.findAll();

       return orderEntities.stream().map(
                order -> {
                    List<ProductOrderedResponse> products =this.productProxy
                            .retrievesOrderedProductsById((
                                    order
                                            .getOrderedProduct()
                                            .stream()
                                            .map(p -> p.getProductId().toString()).toList()));

                    return mapToResponseFromEntity(order, products);
                }
        ).toList();

    }

    public Page<OrderResponse> retrievesOrderPages(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

       final List<OrderResponse> order = this.orderRepository.findAll(pageable).stream().map(orderEntity -> {
            List<ProductOrderedResponse> products =this.productProxy
                    .retrievesOrderedProductsById((
                            orderEntity
                                    .getOrderedProduct()
                                    .stream().map(p -> p.getProductId().toString()).toList()));
            return mapToResponseFromEntity(orderEntity, products);
        }).toList();
        return PageUtils.toPage(order, pageable);
    }

    public OrderChangeStateResponse orderDelivered(Long orderId) {
        final OrderEntity order = this.getOrder(orderId);

        this.orderStateFactory.getState(order.getOrderStatus()).delivered(order);

        return new OrderChangeStateResponse(order.getId(), order.getOrderStatus());
    }

    public OrderChangeStateResponse cancelOrder(Long orderId) {
       final OrderEntity order = getOrder(orderId);

        orderStateFactory.getState(order.getOrderStatus()).cancelled(order);

       List<RestoreProductRequest> productToRestore = order.getOrderedProduct()
                .stream()
                .map(
                        or ->
                                new RestoreProductRequest(or.getProductId(), or.getQuantity())).toList();

        this.handleOrderCancelled(productToRestore );

        return  new OrderChangeStateResponse(order.getId(), order.getOrderStatus());
    }

    private PartialTotalPrice calculateTotalPricePerProduct(OrderedProduct orderedProduct) {
        final BigDecimal partialTotal = orderedProduct
                .getProductUnitPrice()
                .multiply(BigDecimal.valueOf(orderedProduct.getProductQuantity()));

        return new PartialTotalPrice(orderedProduct.getProductId(), partialTotal);
    }

    private BigDecimal calculateTotalPricePerProduct(OrderRequest orderRequest) {
        return orderRequest
                .getOrderedProducts()
                .stream()
                .map(this::calculateTotalPricePerProduct)
                .reduce(BigDecimal.ZERO,
                        (sum, product) -> sum.add(product.totalPrice()),
                        BigDecimal::add);
    }

    private List<String> retrieveProductIdsFromRequest(OrderRequest orderRequest) {
        return orderRequest
                .getOrderedProducts()
                .stream().map(product -> product.getProductId().toString()).toList();
    }

    private OrderEntity getOrder(Long id) {
        return orderRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Order not found"));
    }

    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleOrderCancelled(List<RestoreProductRequest> orderToRestore) {
        this.productProxy.restoreCanceledQuantityProducts(orderToRestore);
    }
}