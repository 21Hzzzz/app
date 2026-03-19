package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class OffShelfSnackRequest {

    @NotNull(message = "卖家ID不能为空")
    private Long sellerId;

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }
}
