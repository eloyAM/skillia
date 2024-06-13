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
    private static final String JWT_REGEX = "^((?:\\.?(?:[A-Za-z0-9-_]+)){3})$";

    @Test
    void loginWithValidCredentialsOkReturnsToken() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"hugo.reyes\", \"password\":  \"1234\"}")
                )
                .andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string(Matchers.matchesRegex(JWT_REGEX)))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithInvalidCredentialsKo4xx() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"username\": \"invented.user\", \"password\":  \"randomPassword\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Could not authenticate"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithInvalidBodySchemaIncludingSomeValidPropKo4xx() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"user\": \"invented.user\", \"password\":  \"randomPassword\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Could not authenticate"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithInvalidBodySchemaKo4xx() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"randomprop1\": \"monday\", \"randomprop2\":  \"tuesday\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andExpect(MockMvcResultMatchers.content().contentTypeCompatibleWith(MediaType.TEXT_PLAIN))
                .andExpect(MockMvcResultMatchers.content().string("Could not authenticate"))
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithContentTypeMissingBodyKo4xx() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginWithBodyMissingContentTypeKoKo4xx() throws Exception {
        mvc.perform(post("/api/auth/login")
                        .content("{\"username\": \"hugo.reyes\", \"password\":  \"1234\"}")
                )
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andDo(MockMvcResultHandlers.print());
    }

    @Test
    void loginMissingContentTypeAndBodyKo4xx() throws Exception {
        mvc.perform(post("/api/auth/login"))
                .andExpect(MockMvcResultMatchers.status().is4xxClientError())
                .andDo(MockMvcResultHandlers.print());
    }
}
