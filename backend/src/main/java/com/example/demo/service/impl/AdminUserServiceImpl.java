package com.example.demo.service.impl;

import com.example.demo.dto.AdminUserRowDTO;
import com.example.demo.entity.User;
import com.example.demo.mapper.AdminUserMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.AdminUserService;
import com.example.demo.vo.AdminUserVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminUserServiceImpl implements AdminUserService {

    private final AdminUserMapper adminUserMapper;
    private final UserMapper userMapper;

    public AdminUserServiceImpl(AdminUserMapper adminUserMapper, UserMapper userMapper) {
        this.adminUserMapper = adminUserMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<AdminUserVO> getAllUsers() {
        List<AdminUserRowDTO> rows = adminUserMapper.selectAllAdminUsers();
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream()
                .map(this::toAdminUserVO)
                .collect(Collectors.toList());
    }

    @Override
    public void deleteUser(Long userId) {
        User user = userMapper.selectById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        if (!"user".equals(user.getRole())) {
            throw new RuntimeException("仅普通用户允许删除");
        }

        Long snackCount = adminUserMapper.countSnacksByUserId(userId);
        if (snackCount != null && snackCount > 0) {
            throw new RuntimeException("该用户存在商品记录，不能删除");
        }

        Long orderCount = adminUserMapper.countOrdersByUserId(userId);
        if (orderCount != null && orderCount > 0) {
            throw new RuntimeException("该用户存在订单记录，不能删除");
        }

        if (adminUserMapper.deleteNormalUserById(userId) <= 0) {
            throw new RuntimeException("删除用户失败");
        }
    }

    private AdminUserVO toAdminUserVO(AdminUserRowDTO row) {
        AdminUserVO response = new AdminUserVO();
        response.setId(row.getId());
        response.setUsername(row.getUsername());
        response.setPhone(row.getPhone());
        response.setRole(row.getRole());
        response.setCreateTime(row.getCreateTime());
        return response;
    }
}
