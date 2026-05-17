package com.raija.auth.dtos;

import java.util.List;

public class CartDto {
    private String userId;
    private java.util.List<CartItemDto> items;
    private double totalAmount;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public List<CartItemDto> getItems() {
        return items;
    }

    public void setItems(List<CartItemDto> items) {
        this.items = items;
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(double totalAmount) {
        this.totalAmount = totalAmount;
    }
}
