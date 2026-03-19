package com.example.demo.controller;

import com.example.demo.dto.CreateSnackRequest;
import com.example.demo.dto.OffShelfSnackRequest;
import com.example.demo.dto.Result;
import com.example.demo.service.SnackService;
import com.example.demo.vo.SnackVO;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/snacks")
public class SnackController {

    private final SnackService snackService;

    public SnackController(SnackService snackService) {
        this.snackService = snackService;
    }

    @GetMapping
    public Result<List<SnackVO>> getAllSnacks() {
        List<SnackVO> response = snackService.getOnSaleSnacks();
        return Result.success("success", response);
    }

    @GetMapping("/mine")
    public Result<List<SnackVO>> getMySnacks(@RequestParam Long sellerId) {
        List<SnackVO> response = snackService.getMySnacks(sellerId);
        return Result.success("success", response);
    }

    @PostMapping
    public Result<SnackVO> createSnack(@Valid @RequestBody CreateSnackRequest request) {
        SnackVO response = snackService.createSnack(request);
        return Result.success("success", response);
    }

    @PatchMapping("/{id}/off-shelf")
    public Result<Void> offShelfSnack(@PathVariable Long id, @Valid @RequestBody OffShelfSnackRequest request) {
        snackService.offShelfSnack(id, request.getSellerId());
        return Result.success("success", null);
    }
}
