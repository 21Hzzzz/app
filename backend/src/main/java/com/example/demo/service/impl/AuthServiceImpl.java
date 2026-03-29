package com.example.demo.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.dto.LoginRequest;
import com.example.demo.dto.LoginResponse;
import com.example.demo.dto.RegisterRequest;
import com.example.demo.dto.SendRegisterSmsCodeRequest;
import com.example.demo.entity.User;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.AuthService;
import com.example.demo.service.SmsVerificationService;
import com.example.demo.vo.SendRegisterSmsCodeVO;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthServiceImpl implements AuthService {

    private final UserMapper userMapper;
    private final PasswordEncoder passwordEncoder;
    private final SmsVerificationService smsVerificationService;

    public AuthServiceImpl(
            UserMapper userMapper,
            PasswordEncoder passwordEncoder,
            SmsVerificationService smsVerificationService
    ) {
        this.userMapper = userMapper;
        this.passwordEncoder = passwordEncoder;
        this.smsVerificationService = smsVerificationService;
    }

    @Override
    public LoginResponse login(LoginRequest request) {
        LambdaQueryWrapper<User> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(User::getUsername, request.getUsername());

        User user = userMapper.selectOne(wrapper);
        if (user == null || !passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new RuntimeException("用户名或密码错误");
        }

        LoginResponse response = new LoginResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setRole(user.getRole());
        return response;
    }

    @Override
    public SendRegisterSmsCodeVO sendRegisterSmsCode(SendRegisterSmsCodeRequest request) {
        return smsVerificationService.sendRegistrationCode(normalizePhone(request.getPhone()));
    }

    @Override
    public boolean register(RegisterRequest request) {
        String username = normalizeText(request.getUsername());
        String phone = normalizePhone(request.getPhone());
        String role = normalizeRole(request.getRole());

        ensureUsernameAvailable(username);
        ensurePhoneAvailable(phone);
        smsVerificationService.verifyRegistrationCode(phone, request.getSmsCode());

        User user = new User();
        user.setUsername(username);
        user.setPhone(phone);
        user.setRole(role);
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));

        return userMapper.insert(user) > 0;
    }

    private void ensureUsernameAvailable(String username) {
        LambdaQueryWrapper<User> usernameWrapper = new LambdaQueryWrapper<>();
        usernameWrapper.eq(User::getUsername, username);
        if (userMapper.selectOne(usernameWrapper) != null) {
            throw new RuntimeException("账号已存在");
        }
    }

    private void ensurePhoneAvailable(String phone) {
        LambdaQueryWrapper<User> phoneWrapper = new LambdaQueryWrapper<>();
        phoneWrapper.eq(User::getPhone, phone);
        if (userMapper.selectOne(phoneWrapper) != null) {
            throw new RuntimeException("手机号已注册");
        }
    }

    private String normalizeRole(String role) {
        String value = role == null || role.isBlank() ? "user" : role.trim();
        if (!"admin".equals(value) && !"user".equals(value)) {
            throw new RuntimeException("无效的角色类型");
        }
        return value;
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private String normalizePhone(String value) {
        return normalizeText(value);
    }
}
