package com.dosmartie;

import com.dosmartie.helper.ResponseMessage;
import com.dosmartie.request.cart.CartOrderRequest;
import com.dosmartie.request.cart.OrderRequest;
import com.dosmartie.request.cart.ProductRequest;
import com.dosmartie.response.cart.OrderResponse;
import com.dosmartie.response.cart.ProductQuantityCheckResponse;
import com.dosmartie.response.cart.ProductResponse;
import com.dosmartie.utils.EncryptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicReference;

import static com.dosmartie.helper.Constants.REMOVE_SPECIAL_CHARACTER_REGEX;
import static com.dosmartie.helper.Urls.PRODUCT_ENDPOINT;

@Service
@Slf4j
public class OrderServiceImpl implements OrderService {
    private final RestTemplate restTemplate;
    private final KafkaTemplate<String, String> kafkaTemplate;
    private final OrderRepository orderRepository;

    private final ResponseMessage<Object> responseMessage;

    private final CartRepository cartRepository;
    private final CartService cartService;
    private final ObjectMapper mapper;
    private final EncryptionUtils encryptionUtils;

    public OrderServiceImpl(RestTemplate restTemplate, KafkaTemplate<String, String> kafkaTemplate, OrderRepository orderRepository, ResponseMessage<Object> responseMessage, CartRepository cartRepository, CartService cartService, ObjectMapper mapper, EncryptionUtils encryptionUtils) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.orderRepository = orderRepository;
        this.responseMessage = responseMessage;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.mapper = mapper;
        this.encryptionUtils = encryptionUtils;
    }

    @Override
    public ResponseEntity<?> placeOrder(CartOrderRequest cartOrderRequest, String email) {
        try {
            log.info("Email set successfully");
            cartOrderRequest.setEmail(email);
            Optional<Cart> optionalCart = cartRepository.findByUserEmail(cartOrderRequest.getEmail().replaceAll(REMOVE_SPECIAL_CHARACTER_REGEX, ""));
            return optionalCart.map(cart -> {
                try {
                    log.info("Cart found");
                    OrderResponse orderResponse = setObjectsForCreatingOrder(cart, cartOrderRequest);
                    if (Objects.isNull(orderResponse.getErrorDesc())) {
                        //clearCartItem(cartOrderRequest.getEmail());
                        return ResponseEntity.ok(responseMessage.setSuccessResponse("Order Placed Successfully", orderResponse));
                    }
                    return ResponseEntity.ok(responseMessage.setSuccessResponse("Order Not Placed", orderResponse));
                } catch (Exception exception) {
                    log.error(exception.fillInStackTrace().getLocalizedMessage());
                    return ResponseEntity.ok(responseMessage.setFailureResponse("No Cart Found", exception));
                }
            }).orElseGet(() -> ResponseEntity.ok(responseMessage.setFailureResponse("No Cart Found")));

        } catch (Exception exception) {
            return ResponseEntity.ok(responseMessage.setFailureResponse("Unable to place order", exception));
        }
    }

    private OrderResponse setObjectsForCreatingOrder(Cart cart, CartOrderRequest cartOrderRequest) {

        OrderResponse orderResponse = mapper.convertValue(orderRepository.save(initiateOrderForTracking(cart, cartOrderRequest)), OrderResponse.class);
        log.info("Saving Order");
        return placeCreateOrder(orderResponse);
    }

    private OrderResponse placeCreateOrder(OrderResponse orderResponse) {
        try {
            if (checkProductOutOfStock(productQuantityCheck(orderResponse.getAvailableProduct()))) {
                log.info("Product stock available");
                reduceStockFromInventory(orderResponse);
                // publishToOrderService(orderRequest);
                return orderResponse;
            } else {
                //todo: If we need to remove and proceed the product // orderRequest.setAvailableProduct(removeItemRegardsOutOfStock(productQuantityCheck(orderRequest.getAvailableProduct())));
                log.warn("Product was out of stock");
                return new OrderResponse("A product in your cart is out of stock");
            }
        } catch (Exception exception) {
            exception.printStackTrace();
            log.error(exception.fillInStackTrace().getLocalizedMessage());
            return null;
        }
    }

    private Order initiateOrderForTracking(Cart cart, CartOrderRequest cartOrderRequest) {
        log.info("Initiating Order for tracking");
        Order order = new Order();
        order.setAvailableProduct(cart.getCartProducts().get(cartOrderRequest.getEmail().replaceAll(REMOVE_SPECIAL_CHARACTER_REGEX, "")));
        order.setOrderedCustomerDetail(cartOrderRequest.getOrderedCustomerDetail());
        AtomicReference<Double> totalOrder = new AtomicReference<>();
        cart.getCartProducts().get(cartOrderRequest.getEmail().replaceAll(REMOVE_SPECIAL_CHARACTER_REGEX, "")).forEach((cartItems) -> totalOrder.set(cartItems.getPrice() * cartItems.getQuantity()));
        order.setTotalOrder(totalOrder.get());
        order.setEmail(cartOrderRequest.getEmail());
        log.info("Order Object created");
        return order;
    }


    private String generateOrderId() {
        String orderCharacter = "ABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
        StringBuilder salt = new StringBuilder();
        Random rnd = new Random();
        while (salt.length() < 18) {
            int index = (int) (rnd.nextFloat() * orderCharacter.length());
            salt.append(orderCharacter.charAt(index));
        }
        return salt.toString();
    }

    private void reduceStockFromInventory(OrderResponse orderResponse) {
        List<ProductRequest> purchaseRequests = new ArrayList<>();
        orderResponse.getAvailableProduct().forEach((product) -> {
            ProductRequest purchaseRequest = new ProductRequest();
            BeanUtils.copyProperties(product, purchaseRequest);
            purchaseRequests.add(purchaseRequest);
        });
        log.info("Made rest template call for stock reduction");
        restTemplate.put(PRODUCT_ENDPOINT, purchaseRequests);
    }

    private boolean checkProductOutOfStock(ProductQuantityCheckResponse[] productQuantityCheckResponses) {
        for (ProductQuantityCheckResponse productQuantityCheckResponse : productQuantityCheckResponses) {
            if (!productQuantityCheckResponse.isAvailable()) {
                return false;
            }
        }
        return true;
    }

    private List<ProductResponse> removeItemRegardsOutOfStock(ProductQuantityCheckResponse[] productQuantityCheckResponses) {
        List<ProductResponse> productResponseList = new ArrayList<>();
        for (ProductQuantityCheckResponse productQuantityCheckResponse : productQuantityCheckResponses) {
            if (productQuantityCheckResponse.isAvailable()) {
                productResponseList.add(productQuantityCheckResponse.getProductResponse());
            }
        }
        return productResponseList;
    }

    //todo: change to Feign
    private synchronized <T> ProductQuantityCheckResponse[] productQuantityCheck(List<T> productRequest) {
        return restTemplate.postForEntity("http://localhost:8042/product/quantity", productRequest, ProductQuantityCheckResponse[].class).getBody();
    }

    // todo: Integrate Kafka
    private void publishToOrderService(OrderRequest orderRequest) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            log.info("Published the order");
            kafkaTemplate.send("mytopic", objectMapper.writeValueAsString(orderRequest));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void clearCartItem(String userEmail) {
        cartService.clearCartItems(userEmail);
    }

}
