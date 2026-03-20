package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

public class SendChatMessageRequest {

    @NotNull(message = "sessionId is required")
    private Long sessionId;

    @NotNull(message = "senderId is required")
    private Long senderId;

    @NotBlank(message = "content cannot be blank")
    @Size(max = 1000, message = "content must be at most 1000 characters")
    private String content;

    public Long getSessionId() {
        return sessionId;
    }

    public void setSessionId(Long sessionId) {
        this.sessionId = sessionId;
    }

    public Long getSenderId() {
        return senderId;
    }

    public void setSenderId(Long senderId) {
        this.senderId = senderId;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
    }
}
