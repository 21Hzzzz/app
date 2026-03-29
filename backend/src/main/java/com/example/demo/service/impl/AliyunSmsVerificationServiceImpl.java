package com.example.demo.service.impl;

import com.aliyun.auth.credentials.Credential;
import com.aliyun.auth.credentials.provider.StaticCredentialProvider;
import com.aliyun.sdk.service.dypnsapi20170525.AsyncClient;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.CheckSmsVerifyCodeResponseBody;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeRequest;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponse;
import com.aliyun.sdk.service.dypnsapi20170525.models.SendSmsVerifyCodeResponseBody;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.example.demo.config.AliyunSmsProperties;
import com.example.demo.entity.User;
import com.example.demo.exception.SmsVerificationException;
import com.example.demo.mapper.UserMapper;
import com.example.demo.service.SmsVerificationService;
import com.example.demo.vo.SendRegisterSmsCodeVO;
import darabonba.core.client.ClientOverrideConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

@Service
public class AliyunSmsVerificationServiceImpl implements SmsVerificationService {

    private static final Logger log = LoggerFactory.getLogger(AliyunSmsVerificationServiceImpl.class);
    private static final String VERIFY_RESULT_PASS = "PASS";
    private static final String RESPONSE_CODE_OK = "OK";
    private static final int MIN_CODE_LENGTH = 4;
    private static final int MAX_CODE_LENGTH = 8;

    private final AliyunSmsProperties properties;
    private final UserMapper userMapper;

    public AliyunSmsVerificationServiceImpl(
            AliyunSmsProperties properties,
            UserMapper userMapper
    ) {
        this.properties = properties;
        this.userMapper = userMapper;
    }

    @Override
    public SendRegisterSmsCodeVO sendRegistrationCode(String phone) {
        String normalizedPhone = normalizePhone(phone);
        validatePhone(normalizedPhone);
        ensurePhoneNotRegistered(normalizedPhone);
        ensureConfigured();

        SendSmsVerifyCodeRequest.Builder requestBuilder = SendSmsVerifyCodeRequest.builder()
                .phoneNumber(normalizedPhone)
                .signName(properties.getSignName().trim())
                .templateCode(properties.getTemplateCode().trim())
                .templateParam(buildTemplateParam())
                .countryCode(properties.getCountryCode())
                .codeLength(properties.getCodeLength().longValue())
                .validTime(properties.getValidMinutes().longValue() * 60L)
                .interval(properties.getIntervalSeconds().longValue())
                .duplicatePolicy(1L)
                .codeType(1L)
                .autoRetry(1L)
                .returnVerifyCode(Boolean.FALSE);

        if (hasText(properties.getSchemeName())) {
            requestBuilder.schemeName(properties.getSchemeName().trim());
        }

        try (AsyncClient client = createClient()) {
            SendSmsVerifyCodeResponse response = await(
                    client.sendSmsVerifyCode(requestBuilder.build()),
                    "send sms verify code",
                    normalizedPhone
            );
            SendSmsVerifyCodeResponseBody body = response.getBody();
            log.info(
                    "Aliyun sms send result statusCode={}, code={}, success={}, requestId={}, message={}",
                    response.getStatusCode(),
                    body == null ? null : body.getCode(),
                    body == null ? null : body.getSuccess(),
                    body == null ? null : body.getRequestId(),
                    body == null ? null : body.getMessage()
            );

            if (!isSuccess(body)) {
                throw new SmsVerificationException(buildSendFailureMessage(body));
            }
        } catch (SmsVerificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Aliyun sms send failed for phone {}", normalizedPhone, e);
            throw new SmsVerificationException("短信服务暂时不可用，请稍后重试", e);
        }

        SendRegisterSmsCodeVO response = new SendRegisterSmsCodeVO();
        response.setCooldownSeconds(properties.getIntervalSeconds());
        response.setExpiresInSeconds(properties.getValidMinutes() * 60);
        response.setCodeLength(properties.getCodeLength());
        return response;
    }

    @Override
    public void verifyRegistrationCode(String phone, String smsCode) {
        String normalizedPhone = normalizePhone(phone);
        String normalizedSmsCode = smsCode == null ? "" : smsCode.trim();
        validatePhone(normalizedPhone);
        validateSmsCode(normalizedSmsCode);
        ensureConfigured();

        CheckSmsVerifyCodeRequest.Builder requestBuilder = CheckSmsVerifyCodeRequest.builder()
                .phoneNumber(normalizedPhone)
                .countryCode(properties.getCountryCode())
                .verifyCode(normalizedSmsCode);

        if (hasText(properties.getSchemeName())) {
            requestBuilder.schemeName(properties.getSchemeName().trim());
        }

        try (AsyncClient client = createClient()) {
            CheckSmsVerifyCodeResponse response = await(
                    client.checkSmsVerifyCode(requestBuilder.build()),
                    "check sms verify code",
                    normalizedPhone
            );
            CheckSmsVerifyCodeResponseBody body = response.getBody();
            log.info(
                    "Aliyun sms verify result statusCode={}, code={}, success={}, verifyResult={}, message={}",
                    response.getStatusCode(),
                    body == null ? null : body.getCode(),
                    body == null ? null : body.getSuccess(),
                    body == null || body.getModel() == null ? null : body.getModel().getVerifyResult(),
                    body == null ? null : body.getMessage()
            );

            if (!isSuccess(body)) {
                throw new SmsVerificationException(buildVerifyFailureMessage(body));
            }

            if (body.getModel() == null || !VERIFY_RESULT_PASS.equalsIgnoreCase(body.getModel().getVerifyResult())) {
                throw new SmsVerificationException("验证码错误或已过期");
            }
        } catch (SmsVerificationException e) {
            throw e;
        } catch (Exception e) {
            log.error("Aliyun sms verify failed for phone {}", normalizedPhone, e);
            throw new SmsVerificationException("短信服务暂时不可用，请稍后重试", e);
        }
    }

    private AsyncClient createClient() {
        Credential credential = Credential.builder()
                .accessKeyId(properties.getAccessKeyId().trim())
                .accessKeySecret(properties.getAccessKeySecret().trim())
                .build();

        return AsyncClient.builder()
                .region(properties.getRegionId().trim())
                .credentialsProvider(StaticCredentialProvider.create(credential))
                .overrideConfiguration(
                        ClientOverrideConfiguration.create()
                                .setEndpointOverride(properties.getEndpoint().trim())
                )
                .build();
    }

    private <T> T await(CompletableFuture<T> future, String action, String phone) throws Exception {
        try {
            return future.get();
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new SmsVerificationException("短信服务请求被中断，请稍后重试", e);
        } catch (ExecutionException e) {
            Throwable cause = e.getCause() == null ? e : e.getCause();
            log.error("Aliyun {} failed for phone {}", action, phone, cause);
            throw new SmsVerificationException("短信服务暂时不可用，请稍后重试", cause);
        }
    }

    private boolean isSuccess(SendSmsVerifyCodeResponseBody body) {
        return body != null
                && Boolean.TRUE.equals(body.getSuccess())
                && RESPONSE_CODE_OK.equalsIgnoreCase(body.getCode());
    }

    private boolean isSuccess(CheckSmsVerifyCodeResponseBody body) {
        return body != null
                && Boolean.TRUE.equals(body.getSuccess())
                && RESPONSE_CODE_OK.equalsIgnoreCase(body.getCode());
    }

    private void ensureConfigured() {
        if (!hasText(properties.getAccessKeyId())
                || !hasText(properties.getAccessKeySecret())
                || !hasText(properties.getSignName())
                || !hasText(properties.getTemplateCode())
                || !hasText(properties.getEndpoint())
                || !hasText(properties.getRegionId())) {
            throw new SmsVerificationException(buildConfigurationMessage());
        }

        if (looksLikePlaceholder(properties.getAccessKeyId())
                || looksLikePlaceholder(properties.getAccessKeySecret())
                || looksLikePlaceholder(properties.getSignName())
                || looksLikePlaceholder(properties.getTemplateCode())) {
            throw new SmsVerificationException(buildConfigurationMessage());
        }

        if (properties.getCodeLength() == null
                || properties.getCodeLength() < MIN_CODE_LENGTH
                || properties.getCodeLength() > MAX_CODE_LENGTH) {
            throw new SmsVerificationException("短信验证码长度配置无效，必须在 4 到 8 位之间");
        }
    }

    private void ensurePhoneNotRegistered(String phone) {
        LambdaQueryWrapper<User> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(User::getPhone, phone);
        if (userMapper.selectOne(queryWrapper) != null) {
            throw new SmsVerificationException("手机号已注册");
        }
    }

    private void validatePhone(String phone) {
        if (!phone.matches("^1\\d{10}$")) {
            throw new SmsVerificationException("请输入 11 位大陆手机号");
        }
    }

    private void validateSmsCode(String smsCode) {
        int codeLength = properties.getCodeLength() == null ? 4 : properties.getCodeLength();
        if (!smsCode.matches("^\\d{" + codeLength + "}$")) {
            throw new SmsVerificationException("请输入 " + codeLength + " 位短信验证码");
        }
    }

    private String normalizePhone(String phone) {
        return phone == null ? "" : phone.trim();
    }

    private String buildTemplateParam() {
        Map<String, String> params = new LinkedHashMap<>();
        params.put(properties.getCodeParamName(), "##code##");
        params.put(properties.getValidMinutesParamName(), String.valueOf(properties.getValidMinutes()));

        StringBuilder builder = new StringBuilder("{");
        boolean first = true;

        for (Map.Entry<String, String> entry : params.entrySet()) {
            if (!first) {
                builder.append(',');
            }
            first = false;
            builder.append('"')
                    .append(escapeJson(entry.getKey()))
                    .append('"')
                    .append(':')
                    .append('"')
                    .append(escapeJson(entry.getValue()))
                    .append('"');
        }

        builder.append('}');
        return builder.toString();
    }

    private String buildConfigurationMessage() {
        return "短信认证服务未配置完成。请在 PNVS 控制台选择赠送签名和赠送模板，并在 application.yml 中填写真实的 access-key-id、access-key-secret、sign-name、template-code。测试阶段仅支持已绑定的测试手机号。";
    }

    private String buildSendFailureMessage(SendSmsVerifyCodeResponseBody body) {
        String message = resolveAliyunMessage(body == null ? null : body.getMessage(), "验证码发送失败，请稍后重试");

        if (containsAny(message, "签名", "模板", "template", "sign")) {
            return message + "。请确认使用的是 PNVS 控制台中的赠送签名和赠送模板，且二者成对使用。";
        }

        if (containsAny(message, "手机号", "mobile", "phone", "授权", "绑定", "test")) {
            return message + "。测试阶段仅支持向 PNVS 控制台中已绑定的测试手机号发送短信。";
        }

        return message;
    }

    private String buildVerifyFailureMessage(CheckSmsVerifyCodeResponseBody body) {
        String message = resolveAliyunMessage(body == null ? null : body.getMessage(), "验证码校验失败，请稍后重试");

        if (containsAny(message, "方案", "scheme")) {
            return message + "。如果发送验证码时使用了方案名称，校验时必须传入相同的方案名称。";
        }

        return message;
    }

    private boolean looksLikePlaceholder(String value) {
        if (!hasText(value)) {
            return true;
        }

        String normalized = value.trim().toLowerCase();
        return normalized.contains("demo")
                || normalized.contains("your-")
                || normalized.contains("example")
                || normalized.contains("placeholder");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }

    private String resolveAliyunMessage(String message, String fallback) {
        if (hasText(message)) {
            return message.trim();
        }
        return fallback;
    }

    private boolean containsAny(String value, String... patterns) {
        if (!hasText(value)) {
            return false;
        }

        String normalized = value.toLowerCase();
        for (String pattern : patterns) {
            if (normalized.contains(pattern.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String escapeJson(String value) {
        String input = value == null ? "" : value;
        return input
                .replace("\\", "\\\\")
                .replace("\"", "\\\"");
    }
}
