package com.example.demo.vo;

import java.util.List;

public class AdminDashboardVO {

    private AdminDashboardSummaryVO summary;
    private List<AdminDashboardRecentOrderVO> recentOrders;

    public AdminDashboardSummaryVO getSummary() {
        return summary;
    }

    public void setSummary(AdminDashboardSummaryVO summary) {
        this.summary = summary;
    }

    public List<AdminDashboardRecentOrderVO> getRecentOrders() {
        return recentOrders;
    }

    public void setRecentOrders(List<AdminDashboardRecentOrderVO> recentOrders) {
        this.recentOrders = recentOrders;
    }
}
