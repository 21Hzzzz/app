package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;

public class MarkChatSessionReadRequest {

    @NotNull(message = "userId is required")
    private Long userId;

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }
}
