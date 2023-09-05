package com.dosmartie.response.cart;

import com.dosmartie.request.cart.ProductRequest;
import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private String userEmail;
    private List<ProductRequest> cartProducts;
}
