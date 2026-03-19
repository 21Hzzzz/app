package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.dto.AdminSnackRowDTO;
import com.example.demo.entity.Snack;
import com.example.demo.mapper.AdminSnackMapper;
import com.example.demo.mapper.SnackMapper;
import com.example.demo.service.AdminSnackService;
import com.example.demo.vo.AdminSnackVO;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class AdminSnackServiceImpl implements AdminSnackService {

    private static final String STATUS_ON_SALE = "on_sale";
    private static final String STATUS_OFF_SHELF = "off_shelf";

    private final AdminSnackMapper adminSnackMapper;
    private final SnackMapper snackMapper;

    public AdminSnackServiceImpl(AdminSnackMapper adminSnackMapper, SnackMapper snackMapper) {
        this.adminSnackMapper = adminSnackMapper;
        this.snackMapper = snackMapper;
    }

    @Override
    public List<AdminSnackVO> getAllSnacks() {
        List<AdminSnackRowDTO> rows = adminSnackMapper.selectAllAdminSnacks();
        if (rows == null || rows.isEmpty()) {
            return Collections.emptyList();
        }

        return rows.stream()
                .map(this::toAdminSnackVO)
                .collect(Collectors.toList());
    }

    @Override
    public void offShelfSnack(Long id) {
        Snack snack = snackMapper.selectById(id);
        if (snack == null) {
            throw new RuntimeException("商品不存在");
        }
        if (!STATUS_ON_SALE.equals(snack.getStatus())) {
            throw new RuntimeException("仅 on_sale 状态的商品允许下架");
        }

        LambdaUpdateWrapper<Snack> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Snack::getId, id)
                .eq(Snack::getStatus, STATUS_ON_SALE)
                .set(Snack::getStatus, STATUS_OFF_SHELF);

        if (snackMapper.update(null, updateWrapper) <= 0) {
            throw new RuntimeException("下架商品失败");
        }
    }

    private AdminSnackVO toAdminSnackVO(AdminSnackRowDTO row) {
        AdminSnackVO response = new AdminSnackVO();
        response.setId(row.getId());
        response.setName(row.getName());
        response.setImage(row.getImage());
        response.setPrice(row.getPrice());
        response.setStock(row.getStock());
        response.setDescription(row.getDescription());
        response.setSellerId(row.getSellerId());
        response.setSellerUsername(row.getSellerUsername());
        response.setStatus(row.getStatus());
        response.setCreateTime(row.getCreateTime());
        return response;
    }
}
