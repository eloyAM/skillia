package com.example.application.view.page;

import com.example.application.view.utils.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H4;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(layout = MainLayout.class, value = "charts/example02")
public class ChartExample02View extends VerticalLayout {

    public ChartExample02View() {
        setSizeFull();
        for (int i = 0; i < 4; i++) {
            add(new H4("Chart " + i));
            add(createElement("echarts-container-" + i, i));
        }
    }

    protected Component createElement(String id, int indexOption) {
        Div echartsContainer = new Div();
        echartsContainer.setId(id);
        echartsContainer.setWidthFull();
        echartsContainer.setHeight("320px");
        echartsContainer.getElement().executeJs(
            "globalThis.skillia.renderSkillsBulletChartExample($0, $1);",
            echartsContainer.getId().orElseThrow(),
            indexOption
        );
        return echartsContainer;
    }
}