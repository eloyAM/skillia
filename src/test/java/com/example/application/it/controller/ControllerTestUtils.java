package com.example.application.it.controller;

import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class ControllerTestUtils {
    public static String getBearerToken(TestRestTemplate testRestTemplate, String jsonCredentials) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> request = new HttpEntity<>(jsonCredentials, headers);
        return testRestTemplate
                .postForEntity("/api/auth/login", request, String.class)
                .getBody();
    }

    public static String getBearerToken(TestRestTemplate testRestTemplate) {
        return getBearerToken(testRestTemplate, Constants.JSON_STR_HR_AUTH_BODY);
    }

    static class Constants {
        public static final String JSON_STR_HR_AUTH_BODY = "{\"username\": \"hugo.reyes\", \"password\": \"1234\"}";
    }
}
