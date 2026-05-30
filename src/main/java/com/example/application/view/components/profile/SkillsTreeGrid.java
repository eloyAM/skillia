package com.example.application.view.components.profile;

import com.example.application.dto.*;
import com.example.application.service.DepartmentService;
import com.example.application.service.PersonSkillService;
import com.vaadin.flow.component.details.Details;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Span;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.treegrid.TreeGrid;
import com.vaadin.flow.theme.lumo.LumoUtility;
import org.springframework.security.core.Authentication;

import java.util.*;
import java.util.stream.Collectors;

public class SkillsTreeGrid extends TreeGrid<AcquiredSkillDto> {
    public SkillsTreeGrid(
        PersonDto person,
        List<AcquiredSkillDto> acquiredSkills,
        Authentication authentication,
        String username,
        PersonSkillService personSkillService,
        DepartmentService departmentService
    ) {
        var tree = this;

        tree.setWidthFull();
        tree.addThemeVariants(GridVariant.LUMO_WRAP_CELL_CONTENT);
        tree.addClassName("person-skills-tree-grid");
        // Horizontal scroll if the screen is not big enough, we don't want to cut the level selector
        tree.setMinWidth("500px");
        // Min height to avoid shrinking due to another section
        tree.setMinHeight("400px");

        tree.addComponentHierarchyColumn(sk -> {
                var skill = sk.getSkill();
                String skillDescriptionStr = skill.getDescription();

                if (sk.getLevel() == -1L) {
                    // Group -> display with description below
                    var container = new VerticalLayout(new Span(skill.getName()));
                    container.addClassName("vaadin-grid-tree-toggle-skill-group-cell-slot");

                    if (skillDescriptionStr != null && !skillDescriptionStr.isBlank()) {
                        var description = new Span(skillDescriptionStr);
                        description.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
                        container.add(description);
                    }
                    return container;
                } else {
                    // Skill -> display using a details component
                    String descText = Optional.ofNullable(skillDescriptionStr).filter(s -> !s.isBlank()).orElse("No description available");
                    Span name = new Span(skill.getName());
                    name.addClassNames(LumoUtility.FontWeight.SEMIBOLD, LumoUtility.TextColor.BODY);
                    var description = new Span(descText);
                    description.addClassNames(LumoUtility.FontSize.SMALL, LumoUtility.TextColor.SECONDARY);
                    return new Details(name, description);
                }
            })
            .setHeader("Skill")
            .setFlexGrow(1);

        boolean isMyProfileOrPermittedRole = new ProfilePermissionsHelper(authentication, username)
            .isMyProfileOrPermittedRole();
        tree.addComponentColumn(v -> {
                if (v.getLevel() == -1) {
                    return new Span();   // Special case for the grouping element
                }
                return new SkillLevelSelector(v.getLevel(), v.getSkill().getId(),
                    username, personSkillService, isMyProfileOrPermittedRole);
            })
            .setHeader("Level")
            .setSortable(true)
            .setComparator(AcquiredSkillDto::getLevel)
            .setAutoWidth(true)
            .setFlexGrow(2);

        DepartmentDto departmentObject = departmentService.findDepartmentByName(person.getDepartment())
            .orElseThrow(() -> new IllegalStateException("Department " + person.getDepartment()
                + " not found for the person " + person.getUsername()));

        // As we show each skill only for the first group where we find it, sorting by ascending number
        // of skills avoids a greater amount of empty groups
        var groupToSkillsMap = departmentObject.getSkillGroups().stream()
            .sorted(Comparator.comparingInt((SkillGroupDto group) -> {
                Set<SkillDto> skills = group.getSkills();
                return (skills == null || skills.isEmpty()) ? Integer.MAX_VALUE : skills.size();
            }))
            .collect(Collectors.toMap(
                skillGroup -> {
                    var skill = SkillDto.builder().id(-1L).name(skillGroup.getName())
                        .description(skillGroup.getDescription()).build();
                    return new AcquiredSkillDto(skill, -1);
                },
                SkillGroupDto::getSkills,
                (a, b) -> a,
                LinkedHashMap::new
            ));
        List<AcquiredSkillDto> rootItems = groupToSkillsMap.keySet().stream().toList();

        var addedSkillsIds = new HashSet<Long>();
        tree.setItems(rootItems, item -> {
            Set<SkillDto> skillsForThisGroup = groupToSkillsMap.get(item);
            if (skillsForThisGroup != null) {
                return skillsForThisGroup.stream()
                    // Avoid adding the same skill even if related to multiple groups
                    // Select the current level for acquired skills or 0 for non-acquired skills
                    .filter(skill -> addedSkillsIds.add(skill.getId()))
                    .map(skill -> {
                        Optional<AcquiredSkillDto> acquiredSkill = acquiredSkills.stream()
                            .filter(as -> as.getSkill().getId().equals(skill.getId()))
                            .findFirst();
                        int level = acquiredSkill.map(AcquiredSkillDto::getLevel).orElse(0);
                        return new AcquiredSkillDto(skill, level);
                    })
                    .toList();
            }
            return List.of();
        });
        tree.expand(rootItems);
    }
}
