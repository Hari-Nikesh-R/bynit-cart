package com.dosmartie;


import com.dosmartie.helper.ResponseMessage;
import com.dosmartie.request.CartRequest;
import com.dosmartie.request.ProductRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.ProductQuantityCheckResponse;
import com.dosmartie.response.ProductResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

@Service
public class CartServiceImpl implements CartService {
    private final CartRepository cartRepository;
    private final ObjectMapper mapper;
    private final ResponseMessage<Object> responseMessage;
    private final ResponseMessage<List<ProductResponse>> listResponseMessage;
    private final RestTemplate restTemplate;

    @Autowired
    public CartServiceImpl(CartRepository cartRepository, ObjectMapper mapper, ResponseMessage<Object> responseMessage, ResponseMessage<List<ProductResponse>> listResponseMessage, RestTemplate restTemplate) {
        this.cartRepository = cartRepository;
        this.mapper = mapper;
        this.responseMessage = responseMessage;
        this.listResponseMessage = listResponseMessage;
        this.restTemplate = restTemplate;
    }

    @Override
    public ResponseEntity<?> addToCart(CartRequest cartRequest) {
        try {
            return getCartProductViaUserEmail(cartRequest.getUserEmail()).map(cart -> {
                try {
                    return ResponseEntity.ok(responseMessage.setSuccessResponse("Cart updated", updateCartItem(cart, cartRequest)));
                } catch (OutOfQuantityException e) {
                    return ResponseEntity.ok(responseMessage.setFailureResponse("Product Not available for purchase", e));
                }
            }).orElseGet(() -> {
                try {
                    return ResponseEntity.ok(responseMessage.setSuccessResponse("Product added to cart", createCart(cartRequest)));
                } catch (OutOfQuantityException e) {
                    return ResponseEntity.ok(responseMessage.setFailureResponse("Product Not available for purchase", e));
                }
            });
        } catch (Exception exception) {
            return ResponseEntity.ok(responseMessage.setFailureResponse("Product is not updated cart", exception));
        }
    }

    @Override
    public BaseResponse<Object> clearCart() {
        try {
            cartRepository.deleteAll();
            return responseMessage.setSuccessResponse("Deleted Cart", null);
        } catch (Exception exception) {
            return responseMessage.setFailureResponse("Cart not cleared");
        }
    }

    @Override
    public synchronized BaseResponse<List<ProductResponse>> clearCartItems(String email) {
        try {
            return getCartProductViaUserEmail(email).map(cart -> {
                cart.getCartProducts().get(email).clear();
                return listResponseMessage.setSuccessResponse("Item removed", migrateCartObjects(cartRepository.save(cart).getUserEmail()));
            }).orElseGet(() -> listResponseMessage.setFailureResponse("No Cart Found"));
        } catch (Exception exception) {
            return listResponseMessage.setFailureResponse("Unable to remove cart", exception);
        }
    }

    @Override
    public BaseResponse<?> deleteItem(String email, String productName) {
        try {
            return getCartProductViaUserEmail(email).map(cart -> {
                ProductResponse productResponse = cart.getCartProducts().get(email).stream().filter((product) -> product.getName().equals(productName)).findFirst().orElse(null);
                if (Objects.nonNull(productResponse)) {
                    cart.getCartProducts().get(email).remove(productResponse);
                    cartRepository.save(cart);
                    return responseMessage.setSuccessResponse("Item deleted", cart.getCartProducts().get(email));
                } else {
                    return responseMessage.setFailureResponse("No such product found in cart");
                }
            }).orElseGet(() -> responseMessage.setFailureResponse("No Cart found"));
        } catch (Exception exception) {
            return responseMessage.setFailureResponse("Unable to delete product", exception);
        }
    }

    @Override
    public BaseResponse<List<ProductResponse>> viewCartItems(String email) {
        try {
            return getCartProductViaUserEmail(email).map(cart -> listResponseMessage.setSuccessResponse("Fetched result", cart.getCartProducts().get(email)))
                    .orElseGet(() -> listResponseMessage.setFailureResponse("No cart is available for the guestID"));
        } catch (Exception exception) {
            return listResponseMessage.setFailureResponse("Unable to fetch data", exception);
        }
    }

    private List<ProductResponse> createCart(CartRequest cartRequest) throws OutOfQuantityException {
        List<ProductRequest> productRequestList = new ArrayList<>();
        productRequestList.add(cartRequest.getCartProduct());
        return getProductAvailabilityAndConstructCart(productRequestList, cartRequest);
    }

    private List<ProductResponse> getProductAvailabilityAndConstructCart(List<ProductRequest> productRequestList, CartRequest cartRequest) {
        String outOfStockError = "";
        List<ProductResponse> productResponses = new ArrayList<>();
        ProductQuantityCheckResponse[] productQuantityCheckResponse = productQuantityCheck(productRequestList);
        for (ProductQuantityCheckResponse quantityCheckResponse : productQuantityCheckResponse) {
            if (quantityCheckResponse.isAvailable()) {
                productResponses = createCartInDb(cartRequest, quantityCheckResponse);
            } else {
                outOfStockError = quantityCheckResponse.getMessage();
            }
        }
        if (productResponses.size() != 0) {
            return productResponses;
        } else {
            throw new OutOfQuantityException(outOfStockError);
        }
    }

    private List<ProductResponse> getProductAvailabilityAndConstructCart(List<ProductRequest> productRequestList, CartRequest cartRequest, Cart cart) {
        String outOfStockError = "";
        Cart cartUpdate = null;
        List<ProductResponse> productResponses = new ArrayList<>();
        ProductQuantityCheckResponse[] productQuantityCheckResponse = productQuantityCheck(productRequestList);
        for (ProductQuantityCheckResponse quantityCheckResponse : productQuantityCheckResponse) {
            if (quantityCheckResponse.isAvailable()) {
                cartUpdate = serializeCartRequest(cartRequest, cart, quantityCheckResponse.getProductResponse());
                productResponses = migrateCartObjects(cartRepository.save(cartUpdate).getUserEmail());
            } else {
                outOfStockError = quantityCheckResponse.getMessage();
            }
        }
        if (productResponses.size() != 0) {
            return productResponses;
        }
        if (outOfStockError.isEmpty()) {
            outOfStockError = "Product not found";
        }
        throw new OutOfQuantityException(outOfStockError);
    }

    private List<ProductResponse> createCartInDb(CartRequest cartRequest, ProductQuantityCheckResponse productQuantityCheckResponse) {
        Cart cart = new Cart();
        Map<String, List<ProductResponse>> cartUpdate = new HashMap<>();
        List<ProductResponse> productRequestList = new ArrayList<>();
        ProductResponse productResponse = productQuantityCheckResponse.getProductResponse();
        double price = productResponse.getPrice();
        BeanUtils.copyProperties(cartRequest.getCartProduct(), productResponse);
        productResponse.setPrice(price);
        productRequestList.add(productResponse);
        cartUpdate.put(cartRequest.getUserEmail(), productRequestList);
        cart.setUserEmail(cartRequest.getUserEmail());
        cart.setCartProducts(cartUpdate);
        return migrateCartObjects(cartRepository.save(cart).getUserEmail());
    }

    private synchronized List<ProductResponse> migrateCartObjects(String guestId) {
        return viewCartItems(guestId).getData();
    }

    //todo: Rest template call to feign
    private synchronized <T> ProductQuantityCheckResponse[] productQuantityCheck(List<T> productRequest) {
        return restTemplate.postForEntity("http://localhost:8042/product/quantity", productRequest, ProductQuantityCheckResponse[].class).getBody();
    }

    private List<ProductResponse> updateCartItem(Cart cart, CartRequest cartRequest) throws OutOfQuantityException {
        List<ProductRequest> productRequestList = new ArrayList<>();
        productRequestList.add(cartRequest.getCartProduct());
        return getProductAvailabilityAndConstructCart(productRequestList, cartRequest, cart);
    }

    private Cart serializeCartRequest(CartRequest cartRequest, Cart cart, ProductResponse productResponse) {
        AtomicBoolean productExist = new AtomicBoolean(false);
        cart.getCartProducts().get(cartRequest.getUserEmail()).forEach((cartItem) -> {
            if (cartItem.getSku().equals(cartRequest.getCartProduct().getSku())) {
                productExist.set(true);
                cartItem.setQuantity(cartRequest.getCartProduct().getQuantity());
                cartItem.setPrice(productResponse.getPrice());
            }
        });
        if (!productExist.get()) {
            double price = productResponse.getPrice();
            BeanUtils.copyProperties(cartRequest.getCartProduct(), productResponse);
            productResponse.setPrice(price);
            cart.getCartProducts().get(cartRequest.getUserEmail()).add(productResponse);
        }
        return cart;
    }

    private synchronized Optional<Cart> getCartProductViaUserEmail(String email) {
        return cartRepository.findByUserEmail(email);
    }
}
