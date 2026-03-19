package com.example.demo.service;

import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;

public interface AuthService {
    LoginResponse login(LoginRequest request);

    boolean register(RegisterRequest request);
}
