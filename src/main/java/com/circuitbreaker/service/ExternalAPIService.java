package com.circuitbreaker.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class ExternalAPIService {
    private final RestTemplate restTemplate;

    @Autowired
    public ExternalAPIService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public String callApi() {
        String var = restTemplate.getForObject("/api/external", String.class);
        System.out.println("call API: " + var);
        return var;
    }
}
