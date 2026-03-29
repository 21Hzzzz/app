package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.SendRegisterSmsCodeRequest;
import com.example.demo.vo.SendRegisterSmsCodeVO;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    SendRegisterSmsCodeVO sendRegisterSmsCode(SendRegisterSmsCodeRequest request);

    boolean register(RegisterRequest request);
}
