package com.example.demo.shared.infrastructure.in;

import org.springdoc.core.customizers.ServerBaseUrlCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration(proxyBeanMethods = false)
public class ServerBaseUrlConfig {
    public static final String SERVER_BASE_URL_CUSTOMIZER = "serverBaseUrlCustomizer";
    public static final String X_GATEWAY_BASE_URL = "X-Gateway-Base-Url";

    @Bean(SERVER_BASE_URL_CUSTOMIZER)
    public ServerBaseUrlCustomizer serverBaseUrlCustomizer() {
        return (serverBaseUrl, request) -> {
            String publicBaseUrl = request.getHeaders().getFirst(X_GATEWAY_BASE_URL);
            if (publicBaseUrl == null || publicBaseUrl.isBlank()) {
                return serverBaseUrl;
            }
            return publicBaseUrl;
        };
    }
}
