package com.dosmartie;

import com.dosmartie.request.cart.CartOrderRequest;
import com.dosmartie.request.cart.CustomerDetailRequest;
import com.dosmartie.request.logistic.DeliveryStatus;
import com.dosmartie.request.logistic.OrderStatus;
import com.dosmartie.request.logistic.PaymentStatus;
import com.dosmartie.response.cart.ProductResponse;
import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.List;

@Data
@Document(collection = "order")
public class Order {
    @Id
    private String orderId;
    private CustomerDetailRequest orderedCustomerDetail;
    private String email;
    private List<ProductResponse> availableProduct;
    private OrderStatus orderStatus = OrderStatus.PENDING;
    private PaymentStatus paymentStatus = PaymentStatus.PAID;
    private DeliveryStatus deliveryStatus = DeliveryStatus.NOT_PICKED;
    private Double totalOrder;

}
