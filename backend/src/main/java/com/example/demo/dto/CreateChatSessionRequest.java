package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class CreateChatSessionRequest {

    @NotNull(message = "orderId is required")
    private Long orderId;

    @NotNull(message = "buyerId is required")
    private Long buyerId;

    @NotNull(message = "sellerId is required")
    private Long sellerId;

    public Long getOrderId() {
        return orderId;
    }

    public void setOrderId(Long orderId) {
        this.orderId = orderId;
    }

    public Long getBuyerId() {
        return buyerId;
    }

    public void setBuyerId(Long buyerId) {
        this.buyerId = buyerId;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}
