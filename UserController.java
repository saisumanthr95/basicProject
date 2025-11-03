package com.example.demo.controller;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class UserController {

    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    @GetMapping("/api/user/dashboard")
    public String userDashboard() {
        return "👤 Welcome to the User Dashboard!";
    }
}
