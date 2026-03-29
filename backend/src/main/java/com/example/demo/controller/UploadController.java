package com.example.demo.controller;

import com.example.demo.dto.Result;
import com.example.demo.service.UploadService;
import com.example.demo.vo.UploadImageVO;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/uploads")
public class UploadController {

    private final UploadService uploadService;

    public UploadController(UploadService uploadService) {
        this.uploadService = uploadService;
    }

    @PostMapping(value = "/images", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public Result<UploadImageVO> uploadImage(@RequestParam("file") MultipartFile file) {
        UploadImageVO response = new UploadImageVO();
        response.setUrl(uploadService.uploadSnackImage(file));
        return Result.success("success", response);
    }
}
