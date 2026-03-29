package com.example.demo.exception;

import com.example.demo.dto.Result;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.MultipartException;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> handleValidException(MethodArgumentNotValidException e) {
        String message = e.getBindingResult()
                .getFieldError()
                .getDefaultMessage();
        return Result.fail(message);
    }

    @ExceptionHandler(FileUploadException.class)
    public Result<String> handleFileUploadException(FileUploadException e) {
        return Result.fail(e.getMessage());
    }

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public Result<String> handleMaxUploadSizeExceededException(MaxUploadSizeExceededException e) {
        return Result.fail("图片大小不能超过 5MB");
    }

    @ExceptionHandler(MultipartException.class)
    public Result<String> handleMultipartException(MultipartException e) {
        return Result.fail("图片上传请求无效");
    }

    @ExceptionHandler(Exception.class)
    public Result<String> handleException(Exception e) {
        return Result.fail("服务器内部错误：" + e.getMessage());
    }
}
