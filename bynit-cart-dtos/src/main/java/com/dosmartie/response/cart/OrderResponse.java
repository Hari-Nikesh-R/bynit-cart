package com.dosmartie.response.cart;

import com.dosmartie.request.cart.CustomerDetailRequest;
import com.dosmartie.request.logistic.DeliveryStatus;
import com.dosmartie.request.logistic.OrderStatus;
import com.dosmartie.request.cart.ProductRequest;
import com.dosmartie.request.logistic.PaymentStatus;
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
    private PaymentStatus paymentStatus = PaymentStatus.PAID;
    private DeliveryStatus deliveryStatus;

    private String errorDesc;

    public OrderResponse(String errorDesc) {
        this.errorDesc = errorDesc;
    }
}
