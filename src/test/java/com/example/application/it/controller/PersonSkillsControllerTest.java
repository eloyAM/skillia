package com.example.application.it.controller;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.PersonWithSkillsDto;
import com.example.application.dto.SkillDto;
import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.client.TestRestTemplate;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.client.RestClient;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.ObjectMapper;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ArrayNode;
import org.testcontainers.shaded.com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class PersonSkillsControllerTest {
    private final WebTestClient wtc;
    private final TestRestTemplate testRestTemplate;
    private String bearerToken = null;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final RestClient restClient;
    private Long personId;

    private Long skillId;

    @Autowired
    public PersonSkillsControllerTest(WebTestClient wtc, TestRestTemplate testRestTemplate) {
        this.wtc = wtc;
        this.testRestTemplate = testRestTemplate;
        restClient = RestClient.create(this.testRestTemplate.getRootUri());
    }

    @BeforeEach
    void setUp() {
        bearerToken = ControllerTestUtils.getBearerToken(testRestTemplate);
    }

    @Test
    void happyPathCrud() throws Exception {
        final String skillName = "Docker";
        final Integer skillLevel = 3;
        final Long skillId = createSkill(skillName);
        final String personId = getFirstPerson();

        // 1st step -> CREATE a skill assigment
        ObjectNode jsonBodyNode = objectMapper.createObjectNode()
                .put("level", skillLevel);

        final String skillAssignationLocation = MessageFormat.format(
                "/api/personSkills/person/{0}/skill/{1}", personId, skillId);
        wtc.put().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(jsonBodyNode))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().location(skillAssignationLocation)
                .expectBody()
                .jsonPath("$.personId").isEqualTo(personId)
                .jsonPath("$.skillId").isEqualTo(skillId)
                .jsonPath("$.level").isEqualTo(skillLevel)
                .consumeWith(System.out::println);

        // 2nd step -> RETRIEVE the created element
        wtc.get().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.level").isEqualTo(skillLevel)
                .jsonPath("$.skill.id").isEqualTo(skillId)
                .consumeWith(System.out::println);

        // 3rd step -> UPDATE the created element (with the PUT idempotency is the same as the creation)
        final Integer skillLevelUpdated = skillLevel + 1;
        jsonBodyNode.put("level", skillLevelUpdated);
        wtc.put().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(jsonBodyNode))
                .exchange()
                .expectStatus().isCreated()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectHeader().location(skillAssignationLocation)
                .expectBody()
                .jsonPath("$.personId").isEqualTo(personId)
                .jsonPath("$.skillId").isEqualTo(skillId)
                .jsonPath("$.level").isEqualTo(skillLevelUpdated)
                .consumeWith(System.out::println);

        wtc.get().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$.level").isEqualTo(skillLevelUpdated)
                .jsonPath("$.skill.id").isEqualTo(skillId)
                .consumeWith(System.out::println);

        // 4th step -> DELETE the element
        wtc.delete().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .exchange()
                .expectStatus().isNoContent()
                .expectBody()
                .consumeWith(System.out::println)
                .isEmpty();

        wtc.get().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .consumeWith(System.out::println)
                .isEmpty();
    }

    @Test
    void bulkCreateRetrieve() throws IOException {
        final String skillName = "Web Components";
        final Integer skillLevel1 = 2;
        final Integer skillLevel2 = 4;
        final Long skillId = createSkill(skillName);
        final String[] usernames = getAvailableUsernames();
        assertThat(usernames).hasSizeGreaterThanOrEqualTo(2);

        // Precondition for later assertion: there are no assignations yet
        wtc.get().uri("/api/personSkills")
                .headers(h -> h.setBearerAuth(bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$[*]").isEmpty();

        // 1st step -> CREATE skill assignments in bulk
        ArrayNode jsonBulkPut = objectMapper.createArrayNode()
                .add(objectMapper.createObjectNode()
                        .put("personId", usernames[0])
                        .put("skillId", skillId)
                        .put("level", skillLevel1)
                )
                .add(objectMapper.createObjectNode()
                        .put("personId", usernames[1])
                        .put("skillId", skillId)
                        .put("level", skillLevel2)
                );

        final String skillAssignationLocation = "/api/personSkills/bulkAssign";
        wtc.put().uri(skillAssignationLocation)
                .headers(h -> h.setBearerAuth(bearerToken))
                .contentType(MediaType.APPLICATION_JSON)
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(objectMapper.writeValueAsString(jsonBulkPut))
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBody()
                .jsonPath("length()").isEqualTo(2)
                .jsonPath("$[0].personId").isEqualTo(usernames[0])
                .jsonPath("$[0].skillId").isEqualTo(skillId)
                .jsonPath("$[0].level").isEqualTo(skillLevel1)
                .jsonPath("$[1].personId").isEqualTo(usernames[1])
                .jsonPath("$[1].skillId").isEqualTo(skillId)
                .jsonPath("$[1].level").isEqualTo(skillLevel2)
                .consumeWith(System.out::println);

        // 2nd step -> RETRIEVE the created elements
        var getAllResponseBody = wtc.get().uri("/api/personSkills")
                .headers(h -> h.setBearerAuth(bearerToken))
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentType(MediaType.APPLICATION_JSON)
                .expectBodyList(PersonWithSkillsDto.class)
                .hasSize(2)
                .value(p -> assertThat(p.stream().map((pp -> pp.getPerson().getUsername())))
                        .containsExactlyInAnyOrder(usernames[0], usernames[1]))
                .consumeWith(System.out::println)
                .returnResult().getResponseBody();

        // Validate the 2 returned elements

        PersonWithSkillsDto getAllResponseBodyPerson1 = getAllResponseBody.stream()
                .filter(p -> usernames[0].equals(p.getPerson().getUsername()))
                .findFirst().orElse(null);
        assertThat(getAllResponseBodyPerson1.getSkills()).hasSize(1);
        assertThat(getAllResponseBodyPerson1.getSkills())
                .first()
                .extracting(AcquiredSkillDto::getSkill)
                .extracting(SkillDto::getId)
                .isEqualTo(skillId);
        assertThat(getAllResponseBodyPerson1.getSkills())
                .first()
                .extracting(AcquiredSkillDto::getLevel)
                .isEqualTo(skillLevel1);

        PersonWithSkillsDto getAllResponseBodyPerson2 = getAllResponseBody.stream()
                .filter(p -> usernames[1].equals(p.getPerson().getUsername()))
                .findFirst().orElse(null);
        assertThat(getAllResponseBodyPerson2.getSkills()).hasSize(1);
        assertThat(getAllResponseBodyPerson2.getSkills())
                .first()
                .extracting(AcquiredSkillDto::getSkill)
                .extracting(SkillDto::getId)
                .isEqualTo(skillId);
        assertThat(getAllResponseBodyPerson2.getSkills())
                .first()
                .extracting(AcquiredSkillDto::getLevel)
                .isEqualTo(skillLevel2);
    }

    //

    private Long createSkill(String skillName) {
        HttpHeaders headers = ControllerTestUtils.createHeaders(h -> {
            h.setBearerAuth(bearerToken);
            h.setContentType(MediaType.APPLICATION_JSON);
            h.setAccept(List.of(MediaType.APPLICATION_JSON));
        });
        HttpEntity<String> request = new HttpEntity<>(
                "{\"name\": \"" + skillName + "\"}",
                headers);
        String createSkillResponseBody = testRestTemplate
                .postForEntity("/api/skill", request, String.class)
                .getBody();
        return JsonPath.parse(createSkillResponseBody).read("$.id", Long.class);
    }

    private String getFirstPerson() {
        String responseBody = restClient.get().uri("/api/person")
                .headers(h -> {
                    h.setBearerAuth(bearerToken);
                    h.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve().body(String.class);
        return JsonPath.parse(responseBody).read("$[0].username", String.class);
    }

    private String[] getAvailableUsernames() {
        String responseBody = restClient.get().uri("/api/person")
                .headers(h -> {
                    h.setBearerAuth(bearerToken);
                    h.setAccept(List.of(MediaType.APPLICATION_JSON));
                })
                .retrieve().body(String.class);
        return JsonPath.parse(responseBody).read("$[*].username", String[].class);
    }

}
