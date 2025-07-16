package com.railway.train_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController{
    @GetMapping("/")
    public String home() {
        return "Welcome to the Railway Train Service!";
    }
}