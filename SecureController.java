package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class SecureController {

    @GetMapping("/api/secure/hello")
    public String hello(Authentication authentication) {
        return "Hello " + authentication.getName() + ", secure access granted! 🚀";
    }
}
