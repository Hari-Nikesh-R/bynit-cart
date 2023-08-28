package com.dosmartie;

import com.dosmartie.request.CartOrderRequest;
import org.springframework.http.ResponseEntity;

public interface OrderService {
    ResponseEntity<?> placeOrder(CartOrderRequest cartOrderRequest);
}
