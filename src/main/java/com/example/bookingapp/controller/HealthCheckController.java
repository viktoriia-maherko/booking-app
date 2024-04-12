package com.example.bookingapp.controller;

import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(value = "/health")
@Tag(name = "API management.",
        description = "Check if API is started.")
public class HealthCheckController {
    @GetMapping
    @ResponseStatus(HttpStatus.OK)
    public String ping() {
        return "Working...";
    }
}
