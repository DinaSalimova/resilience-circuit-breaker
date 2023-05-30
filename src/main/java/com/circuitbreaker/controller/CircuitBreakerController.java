package com.circuitbreaker.controller;

import com.circuitbreaker.service.ExternalAPIService;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api")
public class CircuitBreakerController {

    private final ExternalAPIService externalAPICaller;

    @Autowired
    public CircuitBreakerController(ExternalAPIService externalAPICaller) {
        this.externalAPICaller = externalAPICaller;
    }


    @GetMapping("/circuit-breaker")
    @CircuitBreaker(name = "CircuitBreakerService")
    public String circuitBreakerApi() {
        System.out.println("in circuitBreakerApi");
        return externalAPICaller.callApi();
    }

}
