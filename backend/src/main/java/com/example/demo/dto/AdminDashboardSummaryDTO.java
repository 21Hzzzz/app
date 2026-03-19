package com.example.demo.dto;

import java.math.BigDecimal;

public class AdminDashboardSummaryDTO {

    private Long totalUsers;
    private Long totalSnacks;
    private Long totalOrders;
    private BigDecimal totalAmount;
    private Long onSaleSnacks;
    private Long soldOutSnacks;
    private Long offShelfSnacks;
    private Long normalUsers;
    private Long adminUsers;

    public Long getTotalUsers() {
        return totalUsers;
    }

    public void setTotalUsers(Long totalUsers) {
        this.totalUsers = totalUsers;
    }

    public Long getTotalSnacks() {
        return totalSnacks;
    }

    public void setTotalSnacks(Long totalSnacks) {
        this.totalSnacks = totalSnacks;
    }

    public Long getTotalOrders() {
        return totalOrders;
    }

    public void setTotalOrders(Long totalOrders) {
        this.totalOrders = totalOrders;
    }

    public BigDecimal getTotalAmount() {
        return totalAmount;
    }

    public void setTotalAmount(BigDecimal totalAmount) {
        this.totalAmount = totalAmount;
    }

    public Long getOnSaleSnacks() {
        return onSaleSnacks;
    }

    public void setOnSaleSnacks(Long onSaleSnacks) {
        this.onSaleSnacks = onSaleSnacks;
    }

    public Long getSoldOutSnacks() {
        return soldOutSnacks;
    }

    public void setSoldOutSnacks(Long soldOutSnacks) {
        this.soldOutSnacks = soldOutSnacks;
    }

    public Long getOffShelfSnacks() {
        return offShelfSnacks;
    }

    public void setOffShelfSnacks(Long offShelfSnacks) {
        this.offShelfSnacks = offShelfSnacks;
    }

    public Long getNormalUsers() {
        return normalUsers;
    }

    public void setNormalUsers(Long normalUsers) {
        this.normalUsers = normalUsers;
    }

    public Long getAdminUsers() {
        return adminUsers;
    }

    public void setAdminUsers(Long adminUsers) {
        this.adminUsers = adminUsers;
    }
}
