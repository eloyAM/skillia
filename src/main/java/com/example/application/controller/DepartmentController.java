package com.example.application.controller;

import com.example.application.dto.main.DepartmentDto;
import com.example.application.dto.main.SkillGroupDto;
import com.example.application.service.DepartmentService;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Tag(name = "Department")
@RestController
@RequestMapping("/api/department")
public class DepartmentController {

    private final DepartmentService departmentService;

    public DepartmentController(DepartmentService departmentService) {
        this.departmentService = departmentService;
    }

    @GetMapping("")
    public List<DepartmentDto> getAllDepartments() {
        return departmentService.findAllDepartment();
    }

    @GetMapping("/{id}")
    public DepartmentDto getDepartmentById(@PathVariable Long id) {
        return departmentService.findDepartmentById(id)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @GetMapping("/name/{name}")
    public DepartmentDto getDepartmentByName(@PathVariable String name) {
        return departmentService.findDepartmentByName(name)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    @PutMapping("/{id}/skillGroups")
    public DepartmentDto updateDepartmentSkillGroups(@PathVariable Long id, @RequestBody List<Long> skillGroupIds) {
        DepartmentDto department = getDepartmentById(id);
        department.setSkillGroups(
            skillGroupIds.stream().map(sgId -> SkillGroupDto.builder().id(sgId).build()).toList());
        return departmentService.saveDepartment(department)
            .orElseThrow(() -> new ResponseStatusException(HttpStatus.INTERNAL_SERVER_ERROR));
    }

}
