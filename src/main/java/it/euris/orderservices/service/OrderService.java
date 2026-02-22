package it.euris.orderservices.service;
import it.euris.common.PageUtils;
import it.euris.orderservices.components.OrderStateFactory;
import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.dto.interfaces.ProductProxy;
import it.euris.orderservices.dto.request.OrderRequest;
import it.euris.orderservices.dto.request.RestoreProductRequest;
import it.euris.orderservices.dto.response.OrderChangeStateResponse;
import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.dto.response.PartialTotalPrice;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import it.euris.orderservices.entities.OrderEntity;
import it.euris.orderservices.entities.OrderedProductEntity;
import it.euris.orderservices.repositories.OrderRepository;
import it.euris.orderservices.repositories.OrderedProductRepository;
import it.euris.orderservices.utilities.OrderUtilities;
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



    public OrderService(
            OrderRepository orderRepository,
            ProductProxy productProxy,
            OrderStateFactory orderStateFactory,
            OrderedProductRepository orderedProductRepository
    ) {
        this.orderRepository = orderRepository;
        this.orderStateFactory = orderStateFactory;
        this.productProxy = productProxy;
    }


    public OrderResponse createOrder(OrderRequest orderRequest) {

        final List<ProductOrderedResponse> orderedResponses = this.productProxy
                .retrievesOrderedProducts(( orderRequest.getOrderedProducts()));


        final BigDecimal totalPrice = this.calculateTotalPriceProduct(orderedResponses);

        final OrderEntity orderEntity = new OrderEntity();


        final OrderState order = this.orderStateFactory.getState(OrderStatus.ORDERED);


        orderEntity.setTotalPrice(totalPrice);

        orderEntity.setCustomerId(orderRequest.getCustomerId());

        orderEntity.setOrderStatus(order.getStatus());

       final List<OrderedProductEntity> productList = orderedResponses
                .stream()
                .map(p -> {
                    final OrderedProductEntity orderedProduct = new OrderedProductEntity();

                    orderedProduct.setProductId(p.id());
                    orderedProduct.setQuantity(p.stock());
                    orderedProduct.setProductName(p.productName());

                    orderedProduct.setUnitPrice(p.unitPrice());

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
                    List<ProductOrderedResponse> products = order
                            .getOrderedProduct()
                            .stream()
                            .map(
                                    OrderUtilities::mapToOrderedProduct).toList();

                    return mapToResponseFromEntity(order, products);
                }
        ).toList();

    }

    public Page<OrderResponse> retrievesOrderPages(int page, int size) {

        Pageable pageable = PageRequest.of(page, size);

       final List<OrderResponse> orderEntities = this.orderRepository
               .findAll(pageable)
               .stream().map(orderEntity -> {

                 final List<ProductOrderedResponse> products = orderEntity.getOrderedProduct().stream().map(OrderUtilities::mapToOrderedProduct).toList();
                 return OrderUtilities.mapToResponseFromEntity(orderEntity, products);
               }).toList();

        return PageUtils.toPage(orderEntities, pageable);
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

    private PartialTotalPrice calculatePartialPriceProduct(ProductOrderedResponse orderedProduct) {
        final BigDecimal partialTotal = orderedProduct
                .unitPrice()
                .multiply(BigDecimal.valueOf(orderedProduct.stock()));

        return new PartialTotalPrice(orderedProduct.id(), partialTotal);
    }

    private BigDecimal calculateTotalPriceProduct(List<ProductOrderedResponse> orderRequest) {
        return orderRequest
                .stream()
                .map(this::calculatePartialPriceProduct)
                .reduce(BigDecimal.ZERO,
                        (sum, product) -> sum.add(product.partialPrice()),
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
    public List<Integer> handleOrderCancelled(List<RestoreProductRequest> orderToRestore) {
        return this.productProxy.restoreCanceledQuantityProducts(orderToRestore);
    }
}