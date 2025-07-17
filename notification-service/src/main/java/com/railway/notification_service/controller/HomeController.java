package com.railway.notification_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController{
    @GetMapping("/")
    public String home() {
        return "Welcome to the Railway Notification Service!";
    }
}