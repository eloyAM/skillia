package com.example.application.it.controller;

import com.jayway.jsonpath.JsonPath;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

import java.util.List;
import java.util.function.Consumer;

public class ControllerTestUtils {
    public static String getBearerToken(TestRestTemplate testRestTemplate, String jsonCredentials) {
        HttpHeaders headers = createHeaders(h -> {
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setAccept(List.of(MediaType.APPLICATION_JSON));
        });
        HttpEntity<String> request = new HttpEntity<>(jsonCredentials, headers);
        String body = testRestTemplate
                .postForEntity("/api/auth/login", request, String.class)
                .getBody();
        return JsonPath.read(body, "$.token");
    }

    public static String getBearerToken(TestRestTemplate testRestTemplate) {
        return getBearerToken(testRestTemplate, Constants.JSON_STR_HR_AUTH_BODY);
    }

    public static HttpHeaders createHeaders(Consumer<HttpHeaders> headersConsumer) {
        HttpHeaders headers = new HttpHeaders();
        headersConsumer.accept(headers);
        return headers;
    }

    static class Constants {
        public static final String JSON_STR_HR_AUTH_BODY = "{\"username\": \"hugo.reyes\", \"password\": \"1234\"}";
    }
}
