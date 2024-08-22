package com.kcs.security_sample.security.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "file.upload")
@Getter
@Setter
public class FileUploadProperties {
    private String path;
    private List<String> allowedExtensions;
}
