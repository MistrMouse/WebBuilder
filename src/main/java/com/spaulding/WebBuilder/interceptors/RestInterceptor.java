package com.spaulding.WebBuilder.interceptors;

import jakarta.annotation.Nonnull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.lang.NonNull;

import java.io.IOException;

@Slf4j
public class RestInterceptor implements ClientHttpRequestInterceptor {
    @Override
    @NonNull
    public ClientHttpResponse intercept(HttpRequest request, @Nonnull byte[] body, ClientHttpRequestExecution execution) throws IOException {
        log.info("URI: {}", request.getURI());
        log.info("REST Method: {}", request.getMethod().name());

        long startTime = System.currentTimeMillis();
        ClientHttpResponse response = execution.execute(request, body);
        long endTime = System.currentTimeMillis();

        log.info("Response Status Code: {}", response.getStatusCode().value());
        log.info("Execution Time: {}ms", (endTime - startTime));

        return response;
    }
}
