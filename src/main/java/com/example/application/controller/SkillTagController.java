package com.example.application.controller;

import com.example.application.dto.SkillTagDto;
import com.example.application.service.SkillService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.NotBlank;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.net.URI;
import java.util.List;

@Tag(name = "Skill tags")
@RestController
@RequestMapping("/api/skillTag")
public class SkillTagController {


    private final SkillService skillService;

    public SkillTagController(SkillService skillService) {
        this.skillService = skillService;
    }

    @Operation(description = "Get all skill tags")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved all skill tags")
    })
    @GetMapping("")
    public ResponseEntity<List<SkillTagDto>> getAllSkillTag() {
        return ResponseEntity.ok(skillService.getAllSkillTag());
    }

    @Operation(description = "Get a skill tag by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully retrieved the skill tag"),
        @ApiResponse(responseCode = "404", description = "Element not found",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @GetMapping("/{id}")
    public ResponseEntity<SkillTagDto> getSkillTagById(@PathVariable Long id) {
        return skillService.getSkillTagById(id)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND,
                "No element found for the given ID")
            );
    }

    @Operation(description = "Create a new skill tag")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Successfully created the skill tag"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(responseCode = "409", description = "An element with the same name exists already",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @PostMapping("")
    public ResponseEntity<SkillTagDto> createSkillTag(@RequestBody SkillTagBody body) {
        SkillTagDto skillTagDto = new SkillTagDto().setName(body.name);
        return skillService.saveSkillTag(skillTagDto)
            .map(saved -> ResponseEntity
                .created(URI.create("/api/skillTag/" + saved.getId()))
                .body(saved)
            )
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                "An element with the same name exists already")
            );
    }

    @Operation(description = "Update an existing skill tag")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Successfully updated the skill tag"),
        @ApiResponse(responseCode = "400", description = "Invalid request body",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        ),
        @ApiResponse(responseCode = "409", description = "An element with the same name exists already",
            content = @Content(schema = @Schema(implementation = ProblemDetail.class))
        )
    })
    @PutMapping("/{id}")
    public ResponseEntity<SkillTagDto> updateSkillTag(@PathVariable Long id, @RequestBody SkillTagBody skillTagBody) {
        SkillTagDto skillTagDto = new SkillTagDto(id, skillTagBody.name);
        return skillService.saveSkillTag(skillTagDto)
            .map(ResponseEntity::ok)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.CONFLICT,
                "An element with the same name exists already")
            );
    }

    @Operation(description = "Delete a skill tag by ID")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "Successfully deleted the skill tag")
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteSkillTag(@PathVariable Long id) {
        skillService.deleteSkillTagById(id);
        return ResponseEntity.noContent().build();
    }

    record SkillTagBody(
        @NotBlank String name
    ) {
    }

}