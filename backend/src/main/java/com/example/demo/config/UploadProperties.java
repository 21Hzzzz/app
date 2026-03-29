package com.example.demo.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.file.Path;
import java.nio.file.Paths;

@Component
@ConfigurationProperties(prefix = "app.upload")
public class UploadProperties {

    private String imageDir = "uploads/snacks";
    private String imageUrlPrefix = "/uploads/snacks/";

    public String getImageDir() {
        return imageDir;
    }

    public void setImageDir(String imageDir) {
        this.imageDir = imageDir;
    }

    public String getImageUrlPrefix() {
        String prefix = imageUrlPrefix == null ? "/uploads/snacks/" : imageUrlPrefix.trim();
        if (!prefix.startsWith("/")) {
            prefix = "/" + prefix;
        }
        if (!prefix.endsWith("/")) {
            prefix = prefix + "/";
        }
        return prefix;
    }

    public void setImageUrlPrefix(String imageUrlPrefix) {
        this.imageUrlPrefix = imageUrlPrefix;
    }

    public String getImageUrlPattern() {
        return getImageUrlPrefix() + "**";
    }

    public Path getImageDirectoryPath() {
        return Paths.get(imageDir).toAbsolutePath().normalize();
    }
}
