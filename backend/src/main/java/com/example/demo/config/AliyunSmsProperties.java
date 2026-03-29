package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Component
@ConfigurationProperties(prefix = "app.sms.aliyun")
public class AliyunSmsProperties {

    private String accessKeyId;
    private String accessKeySecret;
    private String regionId = "cn-qingdao";
    private String endpoint = "dypnsapi.aliyuncs.com";
    private String signName;
    private String templateCode;
    private String schemeName;
    private Integer codeLength = 4;
    private Integer validMinutes = 5;
    private Integer intervalSeconds = 60;
    private String countryCode = "86";
    private String codeParamName = "code";
    private String validMinutesParamName = "min";

    public String getAccessKeyId() {
        return accessKeyId;
    }

    public void setAccessKeyId(String accessKeyId) {
        this.accessKeyId = accessKeyId;
    }

    public String getAccessKeySecret() {
        return accessKeySecret;
    }

    public void setAccessKeySecret(String accessKeySecret) {
        this.accessKeySecret = accessKeySecret;
    }

    public String getRegionId() {
        return regionId;
    }

    public void setRegionId(String regionId) {
        this.regionId = regionId;
    }

    public String getEndpoint() {
        return endpoint;
    }

    public void setEndpoint(String endpoint) {
        this.endpoint = endpoint;
    }

    public String getSignName() {
        return signName;
    }

    public void setSignName(String signName) {
        this.signName = signName;
    }

    public String getTemplateCode() {
        return templateCode;
    }

    public void setTemplateCode(String templateCode) {
        this.templateCode = templateCode;
    }

    public String getSchemeName() {
        return schemeName;
    }

    public void setSchemeName(String schemeName) {
        this.schemeName = schemeName;
    }

    public Integer getCodeLength() {
        return codeLength;
    }

    public void setCodeLength(Integer codeLength) {
        this.codeLength = codeLength;
    }

    public Integer getValidMinutes() {
        return validMinutes;
    }

    public void setValidMinutes(Integer validMinutes) {
        this.validMinutes = validMinutes;
    }

    public Integer getIntervalSeconds() {
        return intervalSeconds;
    }

    public void setIntervalSeconds(Integer intervalSeconds) {
        this.intervalSeconds = intervalSeconds;
    }

    public String getCountryCode() {
        return countryCode;
    }

    public void setCountryCode(String countryCode) {
        this.countryCode = countryCode;
    }

    public String getCodeParamName() {
        return codeParamName;
    }

    public void setCodeParamName(String codeParamName) {
        this.codeParamName = codeParamName;
    }

    public String getValidMinutesParamName() {
        return validMinutesParamName;
    }

    public void setValidMinutesParamName(String validMinutesParamName) {
        this.validMinutesParamName = validMinutesParamName;
    }
}
