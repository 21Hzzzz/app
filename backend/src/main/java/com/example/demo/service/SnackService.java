package com.example.demo.service;

import com.example.demo.dto.CreateSnackRequest;
import com.example.demo.vo.SnackVO;

import java.util.List;

public interface SnackService {
    List<SnackVO> getOnSaleSnacks();

    List<SnackVO> getMySnacks(Long sellerId);

    SnackVO createSnack(CreateSnackRequest request);

    void offShelfSnack(Long id, Long sellerId);
}
