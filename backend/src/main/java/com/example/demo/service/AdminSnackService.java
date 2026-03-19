package com.example.demo.service;

import com.example.demo.vo.AdminSnackVO;

import java.util.List;

public interface AdminSnackService {
    List<AdminSnackVO> getAllSnacks();

    void offShelfSnack(Long id);
}
