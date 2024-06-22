package com.example.application.it.controller;

import org.hamcrest.Matchers;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.result.MockMvcResultHandlers;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

@SpringBootTest
@AutoConfigureMockMvc
public class AuthControllerTest {
    @Autowired
    private MockMvc mvc;
    private static final String JWT_REGEX = "^[A-Za-z0-9_-]{2,}(?:\\.[A-Za-z0-9_-]{2,}){2}$";

    @Test
    void loginWithValidCredentialsOkReturnsToken() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"hugo.reyes\", \"password\":  \"1234\"}")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").isString())
                .andExpect(MockMvcResultMatchers.jsonPath("$.token").value(Matchers.matchesRegex(JWT_REGEX)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithInvalidCredentialsKo401() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"invented.user\", \"password\":  \"randomPassword\"}")
                )
                .andExpect(MockMvcResultMatchers.status().isUnauthorized())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("Invalid credentials"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithInvalidBodySchemaIncludingSomeValidPropKo400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\": \"invented.user\", \"password\":  \"randomPassword\"}")
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").isNotEmpty())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithInvalidBodySchemaKo400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"randomprop1\": \"monday\", \"randomprop2\":  \"tuesday\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").isNotEmpty())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithContentTypeMissingBodyKo400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithBodyMissingContentTypeKoKo400() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .content("{\"username\": \"hugo.reyes\", \"password\":  \"1234\"}")
                )
                .andExpect(MockMvcResultMatchers.status().isUnsupportedMediaType())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginMissingContentTypeAndBodyKo400() throws Exception {
        mvc.perform(post("/api/auth/login"))
                .andExpect(MockMvcResultMatchers.status().isBadRequest())
                .andDo(MockMvcResultHandlers.print());
    }
}
