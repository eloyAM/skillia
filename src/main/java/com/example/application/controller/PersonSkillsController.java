package com.example.application.controller;

import com.example.application.dto.*;
import com.example.application.service.PersonSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
public class PersonSkillsController {
    private final PersonSkillService personSkillService;

    public PersonSkillsController(PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Successfully assigned"),
            @ApiResponse(responseCode = "400",
                    description = "Unable to do the assignation. Check that all the required fields are provided")
    })
    @Operation(description =
            "Assign a skill level (1-5) to a person. If the person already has the skill, the level is updated")
    @PutMapping("/assignSkill")
    public ResponseEntity<PersonSkillBasicDto> assignSkill(@Valid @RequestBody PersonSkillBasicDto dto) {
        return Optional.ofNullable(personSkillService.savePersonSkill(dto))
                .map(result -> created(URI.create(MessageFormat.format(
                                "/api/personSkills/person/{0}/skill/{1}", result.getPersonId(), result.getSkillId()))
                        ).body(result)
                )
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unable to do the assignation. Check that all the required fields are provided"));
    }

    @GetMapping("/person/{personId}/skill/{skillId}")
    public ResponseEntity<AcquiredSkillDto> getByPersonIdAndSkillId(
            @PathVariable String personId, @PathVariable Long skillId
    ) {
        return personSkillService.findPersonSkillBasicByPersonIdAndSkillId(new PersonSkillIdDto(personId, skillId))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(description = "Skill assignation in batch")
    @PutMapping("/assignSkillAll")
    public List<PersonSkillBasicDto> assignSkill(@RequestBody Iterable<PersonSkillBasicDto> elements) {
        return personSkillService.savePersonSkill(elements);
    }

    @Operation(description = "Find all skill assignations")
    @GetMapping("/findSkillAssignments")
    public List<PersonWithSkillsDto> findSkillAssignments() {
        return personSkillService.getAllPersonSkill();
    }

    @Operation(description = "Find all the persons and the level for the given skill")
    @GetMapping("/findPersonsBySkill/{skillId}")
    public List<PersonWithLevelDto> findSkillAssignmentsBySkillId(@PathVariable Long skillId) {
        return personSkillService.findAllPersonWithLevelBySkillId(skillId);
    }

    @Operation(description = "Find all the skills and the level for the given person")
    @GetMapping("/findSkillsByPerson/{personId}")
    public List<AcquiredSkillDto> findSkillAssignmentsBySkillId(@PathVariable String personId) {
        return personSkillService.findAllAcquiredSkillByPersonId(personId);
    }

    @Operation(description = "Delete a skill assignment. Success even if the skill was not assigned")
    @DeleteMapping("/skillAssignment")
    public void deleteAssignment(@RequestBody PersonSkillIdDto dto) {
        personSkillService.deletePersonSkillById(dto);
    }
}
