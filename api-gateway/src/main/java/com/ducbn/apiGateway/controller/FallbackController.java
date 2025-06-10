package com.ducbn.apiGateway.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class FallbackController {

    @GetMapping("/authServiceFallback")
    public String authServiceFallback() {
        return "Auth Service is currently unavailable";
    }

    @GetMapping("/busServiceFallback")
    public String busServiceFallback() {
        return "Bus Service is currently unavailable";
    }
}
