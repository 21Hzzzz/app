package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.service.AdminDashboardService;
import com.example.demo.vo.AdminDashboardVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/dashboard")
public class AdminDashboardController {

    private final AdminDashboardService adminDashboardService;

    public AdminDashboardController(AdminDashboardService adminDashboardService) {
        this.adminDashboardService = adminDashboardService;
    }

    @GetMapping
    public Result<AdminDashboardVO> getDashboard() {
        AdminDashboardVO response = adminDashboardService.getDashboard();
        return Result.success("获取成功", response);
    }
}
