package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.example.demo.dto.CreateSnackRequest;
import com.example.demo.entity.Snack;
import com.example.demo.entity.User;
import com.example.demo.mapper.SnackMapper;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.SnackService;
import com.example.demo.vo.SnackVO;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class SnackServiceImpl implements SnackService {

    private static final String STATUS_ON_SALE = "on_sale";
    private static final String STATUS_OFF_SHELF = "off_shelf";

    private final SnackMapper snackMapper;
    private final UserMapper userMapper;

    public SnackServiceImpl(SnackMapper snackMapper, UserMapper userMapper) {
        this.snackMapper = snackMapper;
        this.userMapper = userMapper;
    }

    @Override
    public List<SnackVO> getOnSaleSnacks() {
        LambdaQueryWrapper<Snack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Snack::getStatus, STATUS_ON_SALE)
                .orderByDesc(Snack::getCreateTime);

        List<Snack> snacks = snackMapper.selectList(queryWrapper);
        if (snacks.isEmpty()) {
            return Collections.emptyList();
        }

        return snacks.stream()
                .map(this::toSnackVO)
                .collect(Collectors.toList());
    }

    @Override
    public List<SnackVO> getMySnacks(Long sellerId) {
        LambdaQueryWrapper<Snack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Snack::getSellerId, sellerId)
                .orderByDesc(Snack::getCreateTime);

        List<Snack> snacks = snackMapper.selectList(queryWrapper);
        if (snacks.isEmpty()) {
            return Collections.emptyList();
        }

        return snacks.stream()
                .map(this::toSnackVO)
                .collect(Collectors.toList());
    }

    @Override
    public SnackVO createSnack(CreateSnackRequest request) {
        User seller = userMapper.selectById(request.getSellerId());
        if (seller == null) {
            throw new RuntimeException("当前登录用户不存在");
        }

        Snack snack = new Snack();
        snack.setName(request.getName());
        snack.setPrice(request.getPrice());
        snack.setStock(request.getStock());
        snack.setDescription(normalizeBlank(request.getDescription()));
        snack.setImageUrl(normalizeBlank(request.getImage()));
        snack.setSellerId(seller.getId());
        snack.setStatus(STATUS_ON_SALE);
        snack.setCreateTime(LocalDateTime.now());

        int rows = snackMapper.insert(snack);
        if (rows <= 0) {
            throw new RuntimeException("上架零食失败");
        }

        Snack savedSnack = snackMapper.selectById(snack.getId());
        if (savedSnack == null) {
            throw new RuntimeException("零食已创建但读取失败");
        }

        return toSnackVO(savedSnack);
    }

    @Override
    public void offShelfSnack(Long id, Long sellerId) {
        LambdaQueryWrapper<Snack> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(Snack::getId, id)
                .eq(Snack::getSellerId, sellerId);

        Snack snack = snackMapper.selectOne(queryWrapper);
        if (snack == null) {
            throw new RuntimeException("零食不存在或无权操作");
        }

        LambdaUpdateWrapper<Snack> updateWrapper = new LambdaUpdateWrapper<>();
        updateWrapper.eq(Snack::getId, id)
                .eq(Snack::getSellerId, sellerId)
                .set(Snack::getStatus, STATUS_OFF_SHELF);

        int rows = snackMapper.update(null, updateWrapper);
        if (rows <= 0) {
            throw new RuntimeException("下架零食失败");
        }
    }

    private SnackVO toSnackVO(Snack snack) {
        SnackVO response = new SnackVO();
        response.setId(snack.getId());
        response.setName(snack.getName());
        response.setPrice(snack.getPrice());
        response.setStock(snack.getStock());
        response.setDescription(snack.getDescription());
        response.setSellerId(snack.getSellerId());
        response.setStatus(snack.getStatus());
        response.setCreateTime(snack.getCreateTime());
        response.setImage(snack.getImageUrl());
        return response;
    }

    private String normalizeBlank(String value) {
        if (value == null) {
            return null;
        }

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
