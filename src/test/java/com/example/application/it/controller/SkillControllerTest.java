package com.example.application.it.controller;

import com.example.application.dto.SkillDto;
import com.example.application.it.testutils.CleanDbExtension;
import com.example.application.it.controller.testutils.ControllerTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(CleanDbExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SkillControllerTest {
    private final WebTestClient wtc;
    private final TestRestTemplate testRestTemplate;
    private String bearerToken = null;
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    public SkillControllerTest(WebTestClient wtc, TestRestTemplate testRestTemplate) {
        this.wtc = wtc;
        this.testRestTemplate = testRestTemplate;
    }

    @BeforeEach
    void setUp() {
        bearerToken = ControllerTestUtils.getBearerToken(testRestTemplate);
    }

    @Test
    void happyPathCrud() throws Exception {
        // 1st step -> CREATE a skill
        final String skillName = "Git";
        byte[] postSkillResponseBody = wtc.post().uri("/api/skill")
                .headers(h -> h.setBearerAuth(bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"" + skillName + "\"}")
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo(skillName)
                .jsonPath("$.id").isNumber()
                .consumeWith(System.out::println)
                .returnResult()
                .getResponseBody();
        final SkillDto createdSkill = objectMapper.readValue(postSkillResponseBody, SkillDto.class);
        assertThat(createdSkill.getName()).isEqualTo(skillName);
        assertThat(createdSkill.getId()).isGreaterThan(0);

        // 2nd step -> RETRIEVE the created skill
        wtc.get().uri("/api/skill/{id}", createdSkill.getId())
                .headers(h -> h.setBearerAuth(bearerToken))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo(skillName)
                .jsonPath("$.id").isEqualTo(createdSkill.getId())
                .consumeWith(System.out::println);

        // 3rd step -> UPDATE the created skill
        final String updatedSkillName = "Git SCM";
        wtc.patch().uri("/api/skill/{id}", createdSkill.getId())
                .headers(h -> h.setBearerAuth(bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("{\"name\": \"" + updatedSkillName + "\"}")
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.name").isEqualTo(updatedSkillName)
                .jsonPath("$.id").isEqualTo(createdSkill.getId())
                .consumeWith(System.out::println);

        // 4th step -> DELETE the created skill
        wtc.delete().uri("/api/skill/{id}", createdSkill.getId())
                .headers(h -> h.setBearerAuth(bearerToken))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(System.out::println)
                .isEmpty();

        // 4th step continuation -> check that the skill can't be retrieved after deletion
        wtc.get().uri("/api/skill/{id}", createdSkill.getId())
                .headers(h -> h.setBearerAuth(bearerToken))
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .isEmpty();
    }
}
