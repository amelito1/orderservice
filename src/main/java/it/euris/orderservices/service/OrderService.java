package it.euris.orderservices.service;
import it.euris.common.PageUtils;
import it.euris.orderservices.components.OrderStateFactory;
import it.euris.orderservices.constants.OrderStatus;
import it.euris.orderservices.dto.interfaces.OrderState;
import it.euris.orderservices.dto.interfaces.ProductProxy;
import it.euris.orderservices.dto.request.OrderRequest;
import it.euris.orderservices.dto.request.OrderedProduct;
import it.euris.orderservices.dto.response.OrderChangeStateResponse;
import it.euris.orderservices.dto.response.OrderResponse;
import it.euris.orderservices.dto.response.PartialTotalPrice;
import it.euris.orderservices.dto.response.ProductOrderedResponse;
import it.euris.orderservices.entities.OrderEntity;
import it.euris.orderservices.repositories.OrderRepository;

import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
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
            OrderStateFactory orderStateFactory ) {
        this.orderRepository = orderRepository;
        this.orderStateFactory = orderStateFactory;
        this.productProxy = productProxy;
    }


    public OrderResponse createOrder(OrderRequest orderRequest) {

        final List<ProductOrderedResponse> orderedResponses = this.productProxy.retrievesOrderedProducts(( orderRequest.getOrderedProducts()));

        final BigDecimal totalPrice = this.calculateTotalPricePerProduct(orderRequest);


        final List<String> productsIds = this.retrieveProductIdsFromRequest(orderRequest);

        final OrderEntity orderEntity = new OrderEntity();

        final OrderState order = this.orderStateFactory.getState(OrderStatus.ORDERED);

        orderEntity.setTotalPrice(totalPrice);
        orderEntity.setCustomerId(orderRequest.getCustomerId());
        orderEntity.setProductIds(productsIds);
        orderEntity.setOrderStatus(order.getStatus());

        final OrderEntity savedOrder = this.orderRepository.save(orderEntity);

        return mapToResponseFromEntity(savedOrder, orderedResponses);
    }

    public List<OrderResponse> retrieveOrders() {

        final List<OrderEntity> orderEntities = this.orderRepository.findAll();

       return orderEntities.stream().map(
                order -> {
                    List<ProductOrderedResponse> products =this.productProxy
                            .retrievesOrderedProductsById(( order.getProductIds()));

                    return mapToResponseFromEntity(order, products);
                }
        ).toList();

    }

    public Page<OrderResponse> retrievesOrderPages(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);

       final List<OrderResponse> order = this.orderRepository.findAll(pageable).stream().map(orderEntity -> {
            List<ProductOrderedResponse> products =this.productProxy
                    .retrievesOrderedProductsById(( orderEntity.getProductIds()));
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
        OrderEntity order = getOrder(orderId);
        orderStateFactory.getState(order.getOrderStatus()).cancelled(order);

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
}