package com.example.demo.controller;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.dto.Result;
import com.example.demo.service.OrderService;
import com.example.demo.vo.BoughtOrderVO;
import com.example.demo.vo.CreateOrderVO;
import com.example.demo.vo.SoldRecordVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/orders")
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    @PostMapping
    public Result<CreateOrderVO> createOrder(@Valid @RequestBody CreateOrderRequest request) {
        CreateOrderVO response = orderService.createOrder(request);
        return Result.success("下单成功", response);
    }

    @GetMapping("/bought")
    public Result<List<BoughtOrderVO>> getBoughtOrders(@RequestParam(required = false) Long buyerId) {
        if (buyerId == null) {
            throw new RuntimeException("buyerId 不能为空");
        }

        List<BoughtOrderVO> response = orderService.getBoughtOrders(buyerId);
        return Result.success("获取成功", response);
    }

    @GetMapping("/sold")
    public Result<List<SoldRecordVO>> getSoldRecords(@RequestParam(required = false) Long sellerId) {
        if (sellerId == null) {
            throw new RuntimeException("sellerId 不能为空");
        }

        List<SoldRecordVO> response = orderService.getSoldRecords(sellerId);
        return Result.success("获取成功", response);
    }
}
