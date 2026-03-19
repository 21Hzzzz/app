package com.example.demo.vo;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

public class BoughtOrderVO {

    private Long id;
    private Long buyerId;
    private BigDecimal totalAmount;
    private LocalDateTime createTime;
    private List<BoughtOrderItemVO> items;

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

    public List<BoughtOrderItemVO> getItems() {
        return items;
    }

    public void setItems(List<BoughtOrderItemVO> items) {
        this.items = items;
    }
}
