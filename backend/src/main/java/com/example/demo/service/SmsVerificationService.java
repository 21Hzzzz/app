package com.example.demo.service;

import com.example.demo.vo.SendRegisterSmsCodeVO;

public interface SmsVerificationService {

    SendRegisterSmsCodeVO sendRegistrationCode(String phone);

    void verifyRegistrationCode(String phone, String smsCode);
}
