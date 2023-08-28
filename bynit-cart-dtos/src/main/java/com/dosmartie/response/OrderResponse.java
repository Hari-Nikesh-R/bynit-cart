package com.dosmartie.response;

import com.dosmartie.request.CustomerDetailRequest;
import com.dosmartie.request.OrderStatus;
import com.dosmartie.request.ProductRequest;
import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class OrderResponse {
    private double totalOrder;
    private CustomerDetailRequest orderedCustomerDetail;
    private OrderStatus orderStatus;
    private String orderId;
    private List<ProductRequest> availableProduct;
    private String errorDesc;

    public OrderResponse(String errorDesc) {
        this.errorDesc = errorDesc;
    }
}
