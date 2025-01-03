package com.example.application.controller;

import com.example.application.dto.SkillDto;
import com.example.application.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;
import java.util.Map;

import static org.springframework.http.ResponseEntity.created;

@Tag(name = "Skill")
@RestController
@RequestMapping("/skill")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @Operation(description = "Find all skills")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Existing skills")
    })
    @GetMapping("")
    public List<SkillDto> findAllSkill() {
        return skillService.getAllSkill();
    }

    @Operation(description = "Get a skill by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill found"),
            @ApiResponse(responseCode = "404", description = "Skill not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SkillDto> getSkill(@PathVariable Long id) {
        return skillService.getSkillById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @Operation(description = "Create a skill with the given name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "201", description = "Skill created"),
            @ApiResponse(responseCode = "204", description = "Skill already exists")
    })
    @PostMapping("")
    public ResponseEntity<SkillDto> createSkill(@RequestBody Map<String, String> body) {
        final String skillName = body.get("name");
        return skillService.saveSkill(new SkillDto(skillName))
                .map(skill -> created(URI.create("/api/skill/" + skill.getId())).body(skill))
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(description = "Create the skills with the given names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills created"),
            @ApiResponse(responseCode = "400", description = "Some skill already exists. No element created")
    })
    @PostMapping("/bulk")
    public List<SkillDto> createSkillAll(@RequestBody List<String> body) {
        List<SkillDto> dtoList = body.stream().map(SkillDto::new).toList();
        List<SkillDto> saved = skillService.saveSkill(dtoList);
        if (saved.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some skill already exists. No element created");
        }
        return saved;
    }

    @Operation(description = "Update a skill (name) by id")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill updated"),
            @ApiResponse(responseCode = "400",
                    description = "No skill matching the id or there is already a skill with the same name")
    })
    @PatchMapping("/{id}")
    public SkillDto updateSkill(@PathVariable Long id, @RequestBody Map<String, String> body) { // TODO rename to express that this only updates the name, or modify to update tags as well
        final String skillName = body.get("name");
        return skillService.updateSkill(id, skillName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No skill found to update with the given id " + id));
    }

    @Operation(description = "Delete the skill with the given id. No error thrown if the skill doesn't exist")
    @ApiResponses({
            @ApiResponse(responseCode = "204", description = "Deleted (or not found)")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkill(@PathVariable Long id) {
        skillService.deleteSkillById(id);
        return ResponseEntity.noContent().build();
    }
}
