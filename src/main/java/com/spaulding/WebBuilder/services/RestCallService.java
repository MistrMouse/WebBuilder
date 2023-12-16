package com.spaulding.WebBuilder.services;

import com.spaulding.WebBuilder.exceptions.BadRequestException;
import com.spaulding.WebBuilder.exceptions.ServiceException;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.retry.support.RetryTemplate;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
public class RestCallService {
    @Autowired
    RestTemplate restTemplate;

    @Autowired
    RetryTemplate retryTemplate;

    protected void setRestTemplate(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    protected void setRetryTemplate(RetryTemplate retryTemplate) {
        this.retryTemplate = retryTemplate;
    }

    private ResponseEntity<JSONObject> formatResponse(ResponseEntity<Object> response) {
        JSONObject json = null;
        if (response.getBody() != null && response.getBody() instanceof List<?> body && !body.isEmpty() && body.get(0) instanceof LinkedHashMap<?, ?>) {
            json = new JSONObject();
            int i = 0;
            for (LinkedHashMap<?, ?> item : (List<LinkedHashMap<?, ?>>) body) {
                json.put("" + i, item);
                i++;
            }
        }
        return json == null ? new ResponseEntity<>(response.getBody() == null ? null : (JSONObject) response.getBody(), response.getStatusCode()) : new ResponseEntity<>(json, response.getStatusCode());
    }

    public ResponseEntity<JSONObject> get(String url, HttpHeaders headers, Map<String, Object> params) {
        try {
            initHeaders(headers);
            HttpEntity<String> entity = new HttpEntity<>(headers);
            return formatResponse(retryTemplate.execute(arg0 -> restTemplate.exchange(createUriTemplate(url, params), HttpMethod.GET, entity, Object.class, params == null ? new HashMap<>() : params)));
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException(ex.getMessage());
            }

            throw new ServiceException(ex.getMessage());
        }
    }

    protected ResponseEntity<JSONObject> putOrPost(String url, HttpMethod method, HttpHeaders headers, JSONObject body, Map<String, Object> params) {
        try {
            initHeaders(headers);
            HttpEntity<JSONObject> entity = new HttpEntity<>(body, headers);
            return formatResponse(retryTemplate.execute(arg0 -> restTemplate.exchange(createUriTemplate(url, params), method, entity, Object.class, params == null ? new HashMap<>() : params)));
        }
        catch (HttpClientErrorException ex) {
            if (ex.getStatusCode() == HttpStatus.BAD_REQUEST) {
                throw new BadRequestException(ex.getMessage());
            }

            throw new ServiceException(ex.getMessage());
        }
    }

    private void initHeaders(HttpHeaders headers) {
        if (headers == null) {
            headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
        }
    }

    private String createUriTemplate(String baseUrl, Map<String, Object> params) {
        StringBuilder uriTemplate = new StringBuilder();
        uriTemplate.append(baseUrl);

        if (params != null) {
            uriTemplate.append("?");
            boolean amp = false;
            for (String param : params.keySet()) {
                if (amp) {
                    uriTemplate.append("&");
                }
                uriTemplate.append(param);
                uriTemplate.append("={");
                uriTemplate.append(param);
                uriTemplate.append("}");
                amp = true;
            }
        }

        return uriTemplate.toString();
    }
}
