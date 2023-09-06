package com.dosmartie.controller;

import com.dosmartie.CartService;
import com.dosmartie.helper.ResponseMessage;
import com.dosmartie.helper.Urls;
import com.dosmartie.request.cart.CartRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.cart.ProductResponse;
import com.dosmartie.utils.EncryptionUtils;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.dosmartie.helper.Constants.AUTH_ID;
import static com.dosmartie.helper.Constants.MERCHANT;

@RestController
@RequestMapping(value = Urls.CART)
public class CartController {

    @Autowired
    private CartService cartService;
    @Autowired
    private EncryptionUtils encryptionUtils;

    @Autowired
    private ResponseMessage<List<ProductResponse>> responseMessage;

    @PostMapping(value = Urls.ADD_PRODUCTS)
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartRequest cartRequest, @RequestHeader("email") String email) {
        return cartService.addToCart(cartRequest, email);
    }

    @DeleteMapping
    public BaseResponse<Object> clearCart(@RequestHeader(MERCHANT) String email) {
        return cartService.clearCart(email);
    }

    @DeleteMapping(value = Urls.DELETE_ITEM + "/{itemSku}")
    public BaseResponse<?> deleteCartItem(@RequestHeader("email") String email, @PathVariable("itemSku") String itemSku) {
        return cartService.deleteItem(email, itemSku);
    }

    @DeleteMapping(value = Urls.CLEAR_CART)
    public BaseResponse<List<ProductResponse>> clearCartItem(@RequestHeader("email") String email) {
        return cartService.clearCartItems(email);
    }

    @GetMapping(value = Urls.VIEW)
    public BaseResponse<List<ProductResponse>> getCartItems(@RequestHeader("email") String email) {
        return cartService.viewCartItems(email);
    }
}
