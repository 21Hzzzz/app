package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.service.AdminUserService;
import com.example.demo.vo.AdminUserVO;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/admin/users")
public class AdminUserController {

    private final AdminUserService adminUserService;

    public AdminUserController(AdminUserService adminUserService) {
        this.adminUserService = adminUserService;
    }

    @GetMapping
    public Result<List<AdminUserVO>> getAllUsers() {
        List<AdminUserVO> response = adminUserService.getAllUsers();
        return Result.success("获取成功", response);
    }

    @DeleteMapping("/{id}")
    public Result<Void> deleteUser(@PathVariable Long id) {
        adminUserService.deleteUser(id);
        return Result.success("删除成功", null);
    }
}
