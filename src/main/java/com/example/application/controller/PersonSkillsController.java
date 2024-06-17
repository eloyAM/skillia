package com.example.application.controller;

import com.example.application.dto.*;
import com.example.application.service.PersonSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.text.MessageFormat;
import java.util.List;
import java.util.Optional;

import static org.springframework.http.ResponseEntity.created;

@Tag(name = "Person skills", description = "API for skills assignments")
@RestController
@RequestMapping("/personSkills")
@Validated
public class PersonSkillsController {
    private final PersonSkillService personSkillService;

    public PersonSkillsController(PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
    }

    @Operation(description = "Find all skill assignations")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @GetMapping("")
    public List<PersonWithSkillsDto> findSkillAssignments() {
        return personSkillService.getAllPersonSkill();
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully assigned"),
            @ApiResponse(responseCode = "400",
                    description = "Unable to do the assignation. Check that all the required fields are provided")
    })
    @Operation(description =
            "Assign a skill level (1-5) to a person. If the person already has the skill, the level is updated")
    @PutMapping("/person/{personId}/skill/{skillIid}")
    public ResponseEntity<PersonSkillBasicDto> assignSkill(
            @PathVariable String personId,
            @PathVariable Long skillIid,
            @Valid @RequestBody AssignSkillRequestDto requestBody
    ) {
        final PersonSkillBasicDto data = new PersonSkillBasicDto(personId, skillIid, requestBody.level());
        return Optional.ofNullable(personSkillService.savePersonSkill(data))
                .map(result -> created(URI.create(MessageFormat.format(
                                "/api/personSkills/person/{0}/skill/{1}", result.getPersonId(), result.getSkillId()))
                        ).body(result)
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unable to do the assignation. Check that all the required fields are provided"));
    }

    @Operation(description = "Delete a skill assignment. Success even if the skill was not assigned")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "204", description = "Element deleted or action skipped due to non-existing element"),
    })
    @DeleteMapping("/person/{personId}/skill/{skillId}")
    public ResponseEntity<Void> deleteByPersonIdAndSkillId(
            @PathVariable String personId, @PathVariable Long skillId
    ) {
        personSkillService.deletePersonSkillById(new PersonSkillIdDto(
                personId, skillId
        ));
        return ResponseEntity.noContent().build();
    }

    @Operation(description = "Get the skill assignation (skill level) for the given person and skill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok, element returned"),
            @ApiResponse(responseCode = "404", description = "Element not found")
    })
    @GetMapping("/person/{personId}/skill/{skillId}")
    public ResponseEntity<AcquiredSkillDto> getByPersonIdAndSkillId(
            @PathVariable String personId, @PathVariable Long skillId
    ) {
        return personSkillService.findPersonSkillBasicByPersonIdAndSkillId(new PersonSkillIdDto(personId, skillId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(description = "Bulk skill assignation")
    @PutMapping("bulkAssign")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "All assignments created successfully", content = {
                    @Content(mediaType = MediaType.APPLICATION_JSON_VALUE)
            }),
            @ApiResponse(responseCode = "400", description = "Some elements may contain invalid data, no element saved")
    })
    public List<PersonSkillBasicDto> assignSkillBulk(
            @RequestBody List<@Valid PersonSkillBasicDto> elements
    ) {
        var result = personSkillService.savePersonSkill(elements);
        if (result.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Some elements may contain invalid data, no element saved");
        }
        return result;
    }

    @Operation(description = "Find all the skills and the level for the given person")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @GetMapping("/person/{personId}/skill")
    public List<AcquiredSkillDto> findSkillAssignmentsBySkillId(@PathVariable String personId) {
        return personSkillService.findAllAcquiredSkillByPersonId(personId);
    }

    @Operation(description = "Find all the persons and the level for the given skill")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Ok")
    })
    @GetMapping("/skill/{skillId}")
    public List<PersonWithLevelDto> findSkillAssignmentsBySkillId(@PathVariable Long skillId) {
        return personSkillService.findAllPersonWithLevelBySkillId(skillId);
    }
}
