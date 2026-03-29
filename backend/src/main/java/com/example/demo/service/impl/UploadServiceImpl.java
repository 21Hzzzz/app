package com.example.demo.service.impl;

import com.example.demo.config.UploadProperties;
import com.example.demo.exception.FileUploadException;
import com.example.demo.service.UploadService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

@Service
public class UploadServiceImpl implements UploadService {

    private static final Set<String> ALLOWED_EXTENSIONS = Set.of("jpg", "jpeg", "png", "webp");
    private static final Set<String> ALLOWED_CONTENT_TYPES = Set.of(
            "image/jpeg",
            "image/png",
            "image/webp",
            "image/jpg"
    );

    private final UploadProperties uploadProperties;

    public UploadServiceImpl(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    @Override
    public String uploadSnackImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new FileUploadException("请先选择图片");
        }

        String extension = resolveExtension(file.getOriginalFilename());
        validateContentType(file.getContentType());

        Path uploadDir = uploadProperties.getImageDirectoryPath();
        String filename = UUID.randomUUID() + "." + extension;
        Path target = uploadDir.resolve(filename).normalize();
        if (!target.startsWith(uploadDir)) {
            throw new FileUploadException("图片保存路径无效");
        }

        try {
            Files.createDirectories(uploadDir);
            file.transferTo(target);
        } catch (IOException | IllegalStateException e) {
            throw new FileUploadException("图片上传失败，请稍后重试");
        }

        return uploadProperties.getImageUrlPrefix() + filename;
    }

    private String resolveExtension(String originalFilename) {
        String extension = StringUtils.getFilenameExtension(originalFilename);
        if (!StringUtils.hasText(extension)) {
            throw new FileUploadException("仅支持 jpg、jpeg、png、webp 格式图片");
        }

        String normalizedExtension = extension.toLowerCase(Locale.ROOT);
        if (!ALLOWED_EXTENSIONS.contains(normalizedExtension)) {
            throw new FileUploadException("仅支持 jpg、jpeg、png、webp 格式图片");
        }

        return normalizedExtension;
    }

    private void validateContentType(String contentType) {
        if (!StringUtils.hasText(contentType)) {
            return;
        }

        String normalizedContentType = contentType.toLowerCase(Locale.ROOT);
        if (!ALLOWED_CONTENT_TYPES.contains(normalizedContentType)) {
            throw new FileUploadException("仅支持 jpg、jpeg、png、webp 格式图片");
        }
    }
}
