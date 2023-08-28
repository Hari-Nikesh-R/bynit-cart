package com.dosmartie.response;

import lombok.Data;

@Data
public class ProductQuantityCheckResponse {
    private boolean isAvailable;
    private String message;
    private ProductResponse productResponse;
}
