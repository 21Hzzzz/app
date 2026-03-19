package com.example.demo.service;

import com.example.demo.vo.AdminUserVO;

import java.util.List;

public interface AdminUserService {
    List<AdminUserVO> getAllUsers();

    void deleteUser(Long userId);
}
