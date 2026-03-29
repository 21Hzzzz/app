package com.example.demo.entity;

import com.baomidou.mybatisplus.annotation.IdType;
import com.baomidou.mybatisplus.annotation.TableField;
import com.baomidou.mybatisplus.annotation.TableId;
import com.baomidou.mybatisplus.annotation.TableName;

import java.time.LocalDateTime;

@TableName("chat_session")
public class ChatSession {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("order_id")
    private Long orderId;

    @TableField("buyer_id")
    private Long buyerId;

    @TableField("seller_id")
    private Long sellerId;

    @TableField("buyer_last_read_message_id")
    private Long buyerLastReadMessageId;

    @TableField("seller_last_read_message_id")
    private Long sellerLastReadMessageId;

    @TableField("create_time")
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

    public Long getSellerId() {
        return sellerId;
    }

    public void setSellerId(Long sellerId) {
        this.sellerId = sellerId;
    }

    public Long getBuyerLastReadMessageId() {
        return buyerLastReadMessageId;
    }

    public void setBuyerLastReadMessageId(Long buyerLastReadMessageId) {
        this.buyerLastReadMessageId = buyerLastReadMessageId;
    }

    public Long getSellerLastReadMessageId() {
        return sellerLastReadMessageId;
    }

    public void setSellerLastReadMessageId(Long sellerLastReadMessageId) {
        this.sellerLastReadMessageId = sellerLastReadMessageId;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }
}
