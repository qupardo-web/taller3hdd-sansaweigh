package com.sansaweigh.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class RestClientConfig {

    @Value("${app.external-scale-api.url}")
    private String externalScaleApiUrl;

    @Bean
    public RestClient restClient() {
        return RestClient.create(externalScaleApiUrl);
    }
}
