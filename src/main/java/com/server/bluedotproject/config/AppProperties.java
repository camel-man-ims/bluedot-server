package com.server.bluedotproject.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * app host 주소 가져오기
 */
@Data
@Component
@ConfigurationProperties("app")
public class AppProperties {

    private String host;
}
