package com.example.demo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class AdminDashboardRecentOrderVO {

    private Long id;
    private Long buyerId;
    private String buyerUsername;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private List<AdminDashboardRecentOrderItemVO> items;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public LocalDateTime getCreateTime() {
        return createTime;
    }

    public void setCreateTime(LocalDateTime createTime) {
        this.createTime = createTime;
    }

    public List<AdminDashboardRecentOrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<AdminDashboardRecentOrderItemVO> items) {
        this.items = items;
    }
}
