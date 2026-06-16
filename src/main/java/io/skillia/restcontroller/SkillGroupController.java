package io.skillia.restcontroller;

import io.skillia.dto.main.SkillGroupDto;
import io.skillia.service.SkillService;
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

@Tag(name = "Skill Group")
@RestController
@RequestMapping("/api/skillGroup")
public class SkillGroupController {
    private final SkillService skillService;

    public SkillGroupController(SkillService skillService) {
        this.skillService = skillService;
    }

    @Operation(description = "Retrieve all skill groups")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved skill groups")
    })
    @GetMapping
    public ResponseEntity<List<SkillGroupDto>> getAllSkillGroups() {
        List<SkillGroupDto> skillGroups = skillService.getAllGroups();
        return ResponseEntity.ok(skillGroups);
    }

    @Operation(description = "Retrieve a skill group by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the skill group"),
        @ApiResponse(responseCode = "404", description = "Skill group not found")
    })
    @GetMapping("/{id}")
    public ResponseEntity<SkillGroupDto> getSkillGroupById(@PathVariable Long id) {
        return skillService.getGroupById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No element found for the given ID")
            );
    }

    @Operation(description = "Create a new skill group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the skill group"),
        @ApiResponse(responseCode = "409", description = "Conflict: Skill group with the same name already exists")
    })
    @PostMapping
    public ResponseEntity<SkillGroupDto> createSkillGroup(@RequestBody SkillGroupDto skillGroupDto) {
        return skillService.saveGroup(skillGroupDto)
            .map(saved -> ResponseEntity
                .created(URI.create("/api/skillGroup/" + saved.getId()))
                .body(saved)
            )
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                "An element with the same name exists already")
            );
    }

    @Operation(description = "Update an existing skill group")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the skill group"),
        @ApiResponse(responseCode = "404", description = "Skill group not found"),
        @ApiResponse(responseCode = "409", description = "Conflict: Skill group with the same name already exists")
    })
    @PutMapping("/{id}")
    public ResponseEntity<SkillGroupDto> updateSkillGroup(@PathVariable Long id, @RequestBody SkillGroupDto skillGroupDto) {
        skillGroupDto.setId(id);
        if (skillService.getSkillById(id).isEmpty()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No element found for the given ID");
        }

        return skillService.saveGroup(skillGroupDto)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                "An element with the same name exists already")
            );
    }

    @Operation(description = "Delete a skill group by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted the skill group")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkillGroup(@PathVariable Long id) {
        skillService.deleteGroupById(id);
        return ResponseEntity.noContent().build();
    }
}