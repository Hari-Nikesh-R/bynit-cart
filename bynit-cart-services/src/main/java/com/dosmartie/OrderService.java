package com.dosmartie;

import com.dosmartie.request.cart.CartOrderRequest;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<?> placeOrder(CartOrderRequest cartOrderRequest, String email);
}
