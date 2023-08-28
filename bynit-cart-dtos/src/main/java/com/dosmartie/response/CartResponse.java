package com.dosmartie.response;

import com.dosmartie.request.ProductRequest;
import lombok.Data;

import java.util.List;

@Data
public class CartResponse {
    private String userEmail;
    private List<ProductRequest> cartProducts;
}
