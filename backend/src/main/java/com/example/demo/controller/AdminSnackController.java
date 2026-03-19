package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.service.AdminSnackService;
import com.example.demo.vo.AdminSnackVO;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/snacks")
public class AdminSnackController {

    private final AdminSnackService adminSnackService;

    public AdminSnackController(AdminSnackService adminSnackService) {
        this.adminSnackService = adminSnackService;
    }

    @GetMapping
    public Result<List<AdminSnackVO>> getAllSnacks() {
        List<AdminSnackVO> response = adminSnackService.getAllSnacks();
        return Result.success("success", response);
    }

    @PatchMapping("/{id}/off-shelf")
    public Result<Void> offShelfSnack(@PathVariable Long id) {
        adminSnackService.offShelfSnack(id);
        return Result.success("success", null);
    }
}
