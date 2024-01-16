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

import java.util.List;

@Tag(name = "Skill")
@RestController
@RequestMapping("/skill")
public class SkillController {
    private final SkillService skillService;

    public SkillController(SkillService skillService) {
        this.skillService = skillService;
    }

    @Operation(description = "Find all skills")
    @GetMapping("/find")
    public List<SkillDto> findAllSkill() {
        return skillService.getAllSkill();
    }

    @Operation(description = "Create a skill with the given name")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill created"),
            @ApiResponse(responseCode = "204", description = "Skill already exists")
    })
    @PostMapping("/create")
    public ResponseEntity<SkillDto> createSkill(@RequestBody String skillName) {
        return skillService.saveSkill(new SkillDto(skillName))
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.noContent().build());
    }

    @Operation(description = "Create the skills with the given names")
    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skills created"),
            @ApiResponse(responseCode = "400", description = "Some skill already exists. No element created")
    })
    @PostMapping("/createAll")
    public List<SkillDto> createSkillAll(@RequestBody List<String> body) {
        List<SkillDto> dtoList = body.stream().map(SkillDto::new).toList();
        List<SkillDto> saved = skillService.saveSkill(dtoList);
        if (saved.isEmpty()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Some skill already exists. No element created");
        }
        return saved;
    }

    @ApiResponses(value = {
            @ApiResponse(responseCode = "200", description = "Skill updated"),
            @ApiResponse(responseCode = "400", description = "No skill found to update with the given id")
    })
    @PutMapping("/update/{id}")
    public SkillDto updateSkill(@PathVariable Long id, @RequestBody String skillName) {
        return skillService.updateSkill(id, skillName)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST, "No skill found to update with the given id " + id));
    }

    @Operation(description = "Delete the skill with the given id. No error thrown if the skill doesn't exist")
    @DeleteMapping("/delete/{id}")
    public void deleteSkill(@PathVariable Long id) {
        skillService.deleteSkillById(id);
    }
}
