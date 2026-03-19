package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.service.AdminOrderService;
import com.example.demo.vo.AdminOrderVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/orders")
public class AdminOrderController {

    private final AdminOrderService adminOrderService;

    public AdminOrderController(AdminOrderService adminOrderService) {
        this.adminOrderService = adminOrderService;
    }

    @GetMapping
    public Result<List<AdminOrderVO>> getAllOrders() {
        List<AdminOrderVO> response = adminOrderService.getAllOrders();
        return Result.success("获取成功", response);
    }
}
