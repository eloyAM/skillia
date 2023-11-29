package com.example.application.controller;

import org.springframework.security.access.annotation.Secured;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/home")
public class HomeController {

    public static final String ROLE_HR = "ROLE_HR";

    @GetMapping("/hello")
    public String index() {
        return "Welcome";
    }

    @Secured(ROLE_HR)
    @GetMapping("/hello-restricted")
    public String helloRestricted() {
        return "This is restricted to the roles: " + ROLE_HR;
    }
}
