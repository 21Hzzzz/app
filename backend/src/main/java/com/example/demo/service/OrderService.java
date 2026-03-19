package com.example.demo.service;

import com.example.demo.dto.CreateOrderRequest;
import com.example.demo.vo.BoughtOrderVO;
import com.example.demo.vo.CreateOrderVO;
import com.example.demo.vo.SoldRecordVO;

import java.util.List;

public interface OrderService {
    CreateOrderVO createOrder(CreateOrderRequest request);

    List<BoughtOrderVO> getBoughtOrders(Long buyerId);

    List<SoldRecordVO> getSoldRecords(Long sellerId);
}
