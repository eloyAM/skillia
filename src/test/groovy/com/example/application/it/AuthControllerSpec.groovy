package com.example.application.it

import org.hamcrest.Matchers
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.http.MediaType
import org.springframework.test.web.servlet.MockMvc
import spock.lang.Specification
import spock.lang.Unroll

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*

@SpringBootTest
@AutoConfigureMockMvc
class AuthControllerSpec extends Specification {

    private static final String LOGIN_URL = "/api/auth/login"
    private static final String JWT_REGEX = "^[A-Za-z0-9_-]{2,}(?:\\.[A-Za-z0-9_-]{2,}){2}\$"

    @Autowired
    MockMvc mvc

    def "login with valid credentials returns JWT token"() {
        when:
        def result = loginRequest(
                '{"username":"hugo.reyes","password":"1234"}',
                MediaType.APPLICATION_JSON
        )

        then:
        result.andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.token').isString())
                .andExpect(jsonPath('$.token').value(Matchers.matchesRegex(JWT_REGEX)))
    }

    def "login with invalid credentials returns 401 and error message"() {
        when:
        def result = loginRequest(
                '{"username":"invented.user","password":"randomPassword"}',
                MediaType.APPLICATION_JSON
        )

        then:
        result.andExpect(status().isUnauthorized())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath('$.message').value("Invalid credentials"))
    }

    @Unroll
    def "invalid body returns 4xx (#reason)"() {
        when:
        def result = loginRequest(body, MediaType.APPLICATION_JSON)

        then:
        result.andExpect(statusMatcher)
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_PROBLEM_JSON))
                .andExpect(jsonPath('$.message').isNotEmpty())

        where:
        reason                       | body                                                   | statusMatcher
        "missing username field"     | '{"user":"invented.user","password":"randomPassword"}' | status().isBadRequest()
        "unknown schema fields only" | '{"randomprop1":"monday","randomprop2":"tuesday"}'     | status().is4xxClientError()
    }

    @Unroll
    def "missing content/body combinations return expected status (#reason)"() {
        when:
        def req = post(LOGIN_URL)
        if (contentType != null) req.contentType(contentType)
        if (body != null) req.content(body)

        then:
        mvc.perform(req)
                .andExpect(expectedStatus)

        where:
        reason                        | contentType                | body                                          | expectedStatus
        "content-type but no body"    | MediaType.APPLICATION_JSON | null                                          | status().isBadRequest()
        "body but no content-type"    | null                       | '{"username":"hugo.reyes","password":"1234"}' | status().isUnsupportedMediaType()
        "no content-type and no body" | null                       | null                                          | status().isBadRequest()
    }

    private def loginRequest(String body, MediaType contentType) {
        def req = post(LOGIN_URL)
        if (contentType != null) req.contentType(contentType)
        if (body != null) req.content(body)
        mvc.perform(req)
    }
}