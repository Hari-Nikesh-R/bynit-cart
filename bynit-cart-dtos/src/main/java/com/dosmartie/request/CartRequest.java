package com.dosmartie.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.Data;
import lombok.Setter;

@Data
public class CartRequest {
    @NotNull(message = "userEmail must not be null")
    private String userEmail;
    @NotNull(message = "cartProduct is mandatory")
    @Valid
    private ProductRequest cartProduct;

    public ProductRequest getCartProduct() {
        return cartProduct;
    }
}
