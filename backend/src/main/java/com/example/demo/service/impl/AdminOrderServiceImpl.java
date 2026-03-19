package com.example.demo.service.impl;

import com.example.demo.dto.AdminOrderRowDTO;
import com.example.demo.mapper.AdminOrderMapper;
import com.example.demo.service.AdminOrderService;
import com.example.demo.vo.AdminOrderItemVO;
import com.example.demo.vo.AdminOrderVO;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class AdminOrderServiceImpl implements AdminOrderService {

    private final AdminOrderMapper adminOrderMapper;

    public AdminOrderServiceImpl(AdminOrderMapper adminOrderMapper) {
        this.adminOrderMapper = adminOrderMapper;
    }

    @Override
    public List<AdminOrderVO> getAllOrders() {
        List<AdminOrderRowDTO> rows = adminOrderMapper.selectAllAdminOrderRows();
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        Map<Long, AdminOrderVO> orderMap = new LinkedHashMap<>();
        for (AdminOrderRowDTO row : rows) {
            AdminOrderVO order = orderMap.computeIfAbsent(row.getOrderId(), key -> {
                AdminOrderVO response = new AdminOrderVO();
                response.setId(row.getOrderId());
                response.setBuyerId(row.getBuyerId());
                response.setBuyerUsername(row.getBuyerUsername());
                response.setTotalAmount(row.getTotalAmount());
                response.setCreateTime(row.getCreateTime());
                response.setItems(new ArrayList<>());
                return response;
            });

            if (row.getItemId() != null) {
                AdminOrderItemVO item = new AdminOrderItemVO();
                item.setId(row.getItemId());
                item.setSnackId(row.getSnackId());
                item.setSnackName(row.getSnackName());
                item.setSnackImage(row.getSnackImage());
                item.setSellerId(row.getSellerId());
                item.setSellerUsername(row.getSellerUsername());
                item.setQuantity(row.getQuantity());
                item.setPrice(row.getPrice());
                item.setSubtotal(row.getSubtotal());
                order.getItems().add(item);
            }
        }

        return new ArrayList<>(orderMap.values());
    }
}
