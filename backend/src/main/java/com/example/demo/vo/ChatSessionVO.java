package com.example.demo.vo;

import java.time.LocalDateTime;

public class ChatSessionVO {

    private Long id;
    private Long orderId;
    private Long buyerId;
    private String buyerUsername;
    private Long sellerId;
    private String sellerUsername;
    private Long latestMessageId;
    private Long latestMessageSenderId;
    private String latestMessage;
    private LocalDateTime latestMessageTime;
    private Long unreadCount;
    private LocalDateTime createTime;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

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

    public String getBuyerUsername() {
        return buyerUsername;
    }

    public void setBuyerUsername(String buyerUsername) {
        this.buyerUsername = buyerUsername;
    }

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public String getSellerUsername() {
        return sellerUsername;
    }

    public void setSellerUsername(String sellerUsername) {
        this.sellerUsername = sellerUsername;
    }

    public Long getLatestMessageId() {
        return latestMessageId;
    }

    public void setLatestMessageId(Long latestMessageId) {
        this.latestMessageId = latestMessageId;
    }

    public Long getLatestMessageSenderId() {
        return latestMessageSenderId;
    }

    public void setLatestMessageSenderId(Long latestMessageSenderId) {
        this.latestMessageSenderId = latestMessageSenderId;
    }

    public String getLatestMessage() {
        return latestMessage;
    }

    public void setLatestMessage(String latestMessage) {
        this.latestMessage = latestMessage;
    }

    public LocalDateTime getLatestMessageTime() {
        return latestMessageTime;
    }

    public void setLatestMessageTime(LocalDateTime latestMessageTime) {
        this.latestMessageTime = latestMessageTime;
    }

    public Long getUnreadCount() {
        return unreadCount;
    }

    public void setUnreadCount(Long unreadCount) {
        this.unreadCount = unreadCount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
