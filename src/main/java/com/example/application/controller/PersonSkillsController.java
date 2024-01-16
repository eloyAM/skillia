package com.example.application.controller;

import com.example.application.dto.*;
import com.example.application.service.PersonSkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.Optional;

@Tag(name = "Person skills", description = "API for skills assignments")
@RestController
@RequestMapping("/personSkills")
public class PersonSkillsController {
    private final PersonSkillService personSkillService;

    public PersonSkillsController(PersonSkillService personSkillService) {
        this.personSkillService = personSkillService;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Successfully assigned"),
            @ApiResponse(responseCode = "400",
                    description = "Unable to do the assignation. Check that all the required fields are provided")
    })
    @Operation(description =
            "Assign a skill level (1-5) to a person. If the person already has the skill, the level is updated")
    @PutMapping("/assignSkill")
    public PersonSkillBasicDto assignSkill(@RequestBody PersonSkillBasicDto dto) {
        return Optional.ofNullable(personSkillService.savePersonSkill(dto))
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "Unable to do the assignation. Check that all the required fields are provided"));
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
