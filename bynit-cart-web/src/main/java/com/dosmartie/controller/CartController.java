package com.dosmartie.controller;

import com.dosmartie.CartService;
import com.dosmartie.helper.PropertiesCollector;
import com.dosmartie.helper.ResponseMessage;
import com.dosmartie.helper.Urls;
import com.dosmartie.request.CartRequest;
import com.dosmartie.response.BaseResponse;
import com.dosmartie.response.ProductResponse;
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
    private PropertiesCollector propertiesCollector;

    @Autowired
    private ResponseMessage<List<ProductResponse>> responseMessage;

    @PostMapping(value = Urls.ADD_PRODUCTS)
    public ResponseEntity<?> addToCart(@Valid @RequestBody CartRequest cartRequest, @RequestHeader(AUTH_ID) String authId) {
        return cartService.addToCart(cartRequest, authId);
    }

    @DeleteMapping
    public BaseResponse<Object> clearCart(@RequestHeader(AUTH_ID) String authId, @RequestHeader(MERCHANT) String email) {
        return cartService.clearCart(email, authId);
    }

    @DeleteMapping(value = Urls.DELETE_ITEM + "/{itemSku}")
    public BaseResponse<?> deleteCartItem(@RequestParam("email") String email, @PathVariable("itemSku") String itemSku, @RequestHeader(AUTH_ID) String authId) {
        return cartService.deleteItem(email, itemSku, authId);
    }

    @DeleteMapping(value = Urls.CLEAR_CART)
    public BaseResponse<List<ProductResponse>> clearCartItem(@RequestParam("email") String email, @RequestHeader(AUTH_ID) String authId) {
        if (propertiesCollector.getAuthId().equals(authId)) {
            return cartService.clearCartItems(email);
        }
        return responseMessage.setUnauthorizedResponse("Access denied");
    }

    @GetMapping(value = Urls.VIEW)
    public BaseResponse<List<ProductResponse>> getCartItems(@RequestParam("email") String email, @RequestHeader(AUTH_ID) String authId) {
        if (propertiesCollector.getAuthId().equals(authId)) {
            return cartService.viewCartItems(email);
        }
        return responseMessage.setUnauthorizedResponse("Access denied");
    }
}
