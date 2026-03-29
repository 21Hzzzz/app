package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public class RegisterRequest {

    @NotBlank(message = "请输入账号")
    @Size(min = 3, max = 20, message = "账号长度需在 3 到 20 个字符之间")
    private String username;

    @NotBlank(message = "请输入手机号")
    @Pattern(regexp = "^1\\d{10}$", message = "请输入 11 位大陆手机号")
    private String phone;

    @NotBlank(message = "请输入短信验证码")
    private String smsCode;

    @NotBlank(message = "请输入密码")
    @Size(min = 6, max = 20, message = "密码长度需在 6 到 20 个字符之间")
    private String password;

    private String role = "user";

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getSmsCode() {
        return smsCode;
    }

    public void setSmsCode(String smsCode) {
        this.smsCode = smsCode;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
