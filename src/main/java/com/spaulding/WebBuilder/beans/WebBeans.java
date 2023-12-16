package com.spaulding.WebBuilder.beans;

import com.spaulding.WebBuilder.interceptors.RestInterceptor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestTemplate;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Configuration
public class WebBeans {
    @Bean
    public RestTemplate getRestTemplate() {
        List<ClientHttpRequestInterceptor> interceptors = new ArrayList<>();
        interceptors.add(new RestInterceptor());

        List<HttpMessageConverter<?>> messageConverters = new ArrayList<>();
        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        converter.setSupportedMediaTypes(Collections.singletonList(MediaType.ALL));
        messageConverters.add(converter);

        RestTemplate restTemplate = new RestTemplate();
        restTemplate.setInterceptors(interceptors);
        restTemplate.setMessageConverters(messageConverters);

        return restTemplate;
    }

    @Bean
    public RetryTemplate getRetryTemplate() {
        return RetryTemplate.builder()
                .maxAttempts(3)
                .fixedBackoff(1000)
                .retryOn(RestClientResponseException.class)
                .build();
    }

    @Bean
    public DefaultRedirectStrategy getDefaultRedirectStrategy() {
        return new DefaultRedirectStrategy();
    }
}
