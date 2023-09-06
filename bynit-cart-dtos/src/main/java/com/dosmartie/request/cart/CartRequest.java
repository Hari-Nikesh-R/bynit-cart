package com.dosmartie.request.cart;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class CartRequest {
    private String userEmail;
    @NotNull(message = "cartProduct is mandatory")
    @Valid
    private ProductRequest cartProduct;
    public ProductRequest getCartProduct() {
        return cartProduct;
    }
}
