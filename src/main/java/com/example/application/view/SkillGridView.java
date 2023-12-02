package com.example.application.view;

import com.example.application.dto.SkillDto;
import com.example.application.service.SkillService;
import com.vaadin.flow.component.grid.Grid;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

import java.util.List;

@PermitAll
@Route(layout = MainLayout.class, value = "skillgrid")
public class SkillGridView extends VerticalLayout {

    private final SkillService skillService;

    public SkillGridView(SkillService skillService) {
        this.skillService = skillService;
        createUi();
    }

    private void createUi() {
        setSizeFull();
        Grid<SkillDto> skillGrid = new Grid<>(SkillDto.class, false);
        skillGrid.addColumn(SkillDto::getId).setHeader("Id");
        skillGrid.addColumn(SkillDto::getName).setHeader("Name");

        List<SkillDto> skillAll = skillService.getAllSkill();
        skillGrid.setItems(skillAll);

        add(skillGrid);
    }

}
