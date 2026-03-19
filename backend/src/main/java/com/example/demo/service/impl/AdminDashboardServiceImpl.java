package com.example.demo.service.impl;

import com.example.demo.dto.AdminDashboardRecentOrderRowDTO;
import com.example.demo.dto.AdminDashboardSummaryDTO;
import com.example.demo.mapper.AdminDashboardMapper;
import com.example.demo.service.AdminDashboardService;
import com.example.demo.vo.AdminDashboardRecentOrderItemVO;
import com.example.demo.vo.AdminDashboardRecentOrderVO;
import com.example.demo.vo.AdminDashboardSummaryVO;
import com.example.demo.vo.AdminDashboardVO;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminDashboardServiceImpl implements AdminDashboardService {

    private final AdminDashboardMapper adminDashboardMapper;

    public AdminDashboardServiceImpl(AdminDashboardMapper adminDashboardMapper) {
        this.adminDashboardMapper = adminDashboardMapper;
    }

    @Override
    public AdminDashboardVO getDashboard() {
        AdminDashboardVO response = new AdminDashboardVO();
        response.setSummary(buildSummary());
        response.setRecentOrders(buildRecentOrders());
        return response;
    }

    private AdminDashboardSummaryVO buildSummary() {
        AdminDashboardSummaryDTO row = adminDashboardMapper.selectDashboardSummary();
        AdminDashboardSummaryVO summary = new AdminDashboardSummaryVO();
        if (row == null) {
            summary.setTotalUsers(0L);
            summary.setTotalSnacks(0L);
            summary.setTotalOrders(0L);
            summary.setTotalAmount(BigDecimal.ZERO);
            summary.setOnSaleSnacks(0L);
            summary.setSoldOutSnacks(0L);
            summary.setOffShelfSnacks(0L);
            summary.setNormalUsers(0L);
            summary.setAdminUsers(0L);
            return summary;
        }

        summary.setTotalUsers(defaultLong(row.getTotalUsers()));
        summary.setTotalSnacks(defaultLong(row.getTotalSnacks()));
        summary.setTotalOrders(defaultLong(row.getTotalOrders()));
        summary.setTotalAmount(row.getTotalAmount() == null ? BigDecimal.ZERO : row.getTotalAmount());
        summary.setOnSaleSnacks(defaultLong(row.getOnSaleSnacks()));
        summary.setSoldOutSnacks(defaultLong(row.getSoldOutSnacks()));
        summary.setOffShelfSnacks(defaultLong(row.getOffShelfSnacks()));
        summary.setNormalUsers(defaultLong(row.getNormalUsers()));
        summary.setAdminUsers(defaultLong(row.getAdminUsers()));
        return summary;
    }

    private List<AdminDashboardRecentOrderVO> buildRecentOrders() {
        List<AdminDashboardRecentOrderRowDTO> rows = adminDashboardMapper.selectRecentOrderRows();
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, AdminDashboardRecentOrderVO> orderMap = new LinkedHashMap<>();
        for (AdminDashboardRecentOrderRowDTO row : rows) {
            AdminDashboardRecentOrderVO order = orderMap.computeIfAbsent(row.getOrderId(), key -> {
                AdminDashboardRecentOrderVO recentOrder = new AdminDashboardRecentOrderVO();
                recentOrder.setId(row.getOrderId());
                recentOrder.setBuyerId(row.getBuyerId());
                recentOrder.setBuyerUsername(row.getBuyerUsername());
                recentOrder.setTotalAmount(row.getTotalAmount());
                recentOrder.setCreateTime(row.getCreateTime());
                recentOrder.setItems(new ArrayList<>());
                return recentOrder;
            });

            if (row.getItemId() != null) {
                AdminDashboardRecentOrderItemVO item = new AdminDashboardRecentOrderItemVO();
                item.setId(row.getItemId());
                item.setSnackId(row.getSnackId());
                item.setSnackName(row.getSnackName());
                item.setQuantity(row.getQuantity());
                item.setPrice(row.getPrice());
                item.setSubtotal(row.getSubtotal());
                order.getItems().add(item);
            }
        }

        return new ArrayList<>(orderMap.values());
    }

    private Long defaultLong(Long value) {
        return value == null ? 0L : value;
    }
}
