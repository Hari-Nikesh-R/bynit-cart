package com.dosmartie;

import com.dosmartie.helper.PropertiesCollector;
import com.dosmartie.helper.ResponseMessage;
import com.dosmartie.request.CartOrderRequest;
import com.dosmartie.request.OrderRequest;
import com.dosmartie.request.OrderStatus;
import com.dosmartie.request.ProductRequest;
import com.dosmartie.response.OrderResponse;
import com.dosmartie.response.ProductQuantityCheckResponse;
import com.dosmartie.response.ProductResponse;
import com.dosmartie.utils.EncryptionUtils;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
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

    private final ResponseMessage<Object> responseMessage;

    private final CartRepository cartRepository;
    private final CartService cartService;


   private final EncryptionUtils encryptionUtils;
    @Autowired
    public OrderServiceImpl(RestTemplate restTemplate, KafkaTemplate<String, String> kafkaTemplate, ResponseMessage<Object> responseMessage, CartRepository cartRepository, CartService cartService, EncryptionUtils encryptionUtils) {
        this.restTemplate = restTemplate;
        this.kafkaTemplate = kafkaTemplate;
        this.responseMessage = responseMessage;
        this.cartRepository = cartRepository;
        this.cartService = cartService;
        this.encryptionUtils = encryptionUtils;
    }

    @Override
    public ResponseEntity<?> placeOrder(CartOrderRequest cartOrderRequest, String authId) {
        try {
            if (encryptionUtils.decryptAuthIdAndValidateRequest(authId)) {
                Optional<Cart> optionalCart = cartRepository.findByUserEmail(cartOrderRequest.getEmail().replaceAll(REMOVE_SPECIAL_CHARACTER_REGEX, ""));
                return optionalCart.map(cart -> {
                    try {
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
            }
            else {
                return ResponseEntity.ok(responseMessage.setUnauthorizedResponse("Access denied"));
            }
        } catch (Exception exception) {
            return ResponseEntity.ok(responseMessage.setFailureResponse("Unable to place order", exception));
        }
    }

    private OrderResponse setObjectsForCreatingOrder(Cart cart, CartOrderRequest cartOrderRequest) {
        OrderRequest orderRequest = new OrderRequest();
        orderRequest.setOrderStatus(OrderStatus.COMPLETED);
        orderRequest.setAvailableProduct(cart.getCartProducts().get(cartOrderRequest.getEmail().replaceAll(REMOVE_SPECIAL_CHARACTER_REGEX, "")));
        orderRequest.setOrderedCustomerDetail(cartOrderRequest.getOrderedCustomerDetail());
        AtomicReference<Double> totalOrder = new AtomicReference<>();
        cart.getCartProducts().get(cartOrderRequest.getEmail().replaceAll(REMOVE_SPECIAL_CHARACTER_REGEX, "")).forEach((cartItems) -> totalOrder.set(cartItems.getPrice() * cartItems.getQuantity()));
        orderRequest.setTotalOrder(totalOrder.get());
        orderRequest.setEmail(cartOrderRequest.getEmail());
        return createOrder(orderRequest);
    }

    private OrderResponse createOrder(OrderRequest orderRequest) {
        try {
            if (checkProductOutOfStock(productQuantityCheck(orderRequest.getAvailableProduct()))) {
                reduceStockFromInventory(orderRequest);
                orderRequest.setOrderId(generateOrderId());
                publishToOrderService(orderRequest);
                OrderResponse orderResponse = new OrderResponse();
                BeanUtils.copyProperties(orderRequest, orderResponse);
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

    private void reduceStockFromInventory(OrderRequest orderRequest) {
        List<ProductRequest> purchaseRequests = new ArrayList<>();
        orderRequest.getAvailableProduct().forEach((product) -> {
            ProductRequest purchaseRequest = new ProductRequest();
            BeanUtils.copyProperties(product, purchaseRequest);
            purchaseRequests.add(purchaseRequest);
        });
        //todo: change Rest template to Feign
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
            kafkaTemplate.send("mytopic", objectMapper.writeValueAsString(orderRequest));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private synchronized void clearCartItem(String userEmail) {
        cartService.clearCartItems(userEmail);
    }

}
