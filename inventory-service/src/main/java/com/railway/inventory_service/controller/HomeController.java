package com.railway.inventory_service.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HomeController{
    @GetMapping("/")
    public String home() {
        return "Welcome to the Railway Inventory Service!";
    }
}