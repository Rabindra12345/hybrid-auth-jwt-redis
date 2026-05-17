package com.raija.auth.dtos;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;

public class AddToCartRequest {

    @NotNull
    private String productId;

    @Min(1)
    private int quantity;

    public String getProductId() {
        return productId;
    }

    public void setProductId(String productId) {
        this.productId = productId;
    }

    public int getQuantity() {
        return quantity;
    }

    public void setQuantity(int quantity) {
        this.quantity = quantity;
    }
}
