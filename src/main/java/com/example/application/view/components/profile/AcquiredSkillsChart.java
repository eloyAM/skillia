package com.example.application.view.components.profile;

import com.example.application.dto.AcquiredSkillDto;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.JsonArray;

import java.util.ArrayList;
import java.util.List;

import static com.example.application.view.utils.JsonUtils.toJsonArray;

public class AcquiredSkillsChart extends VerticalLayout {

    public AcquiredSkillsChart(boolean useChartJs, boolean useEcharts, List<AcquiredSkillDto> acquiredSkills) {
        VerticalLayout wrapper = this;
        wrapper.addClassName("user-profile-charts-layout");
        wrapper.setPadding(false);
        wrapper.setSpacing(true);
        wrapper.setWidthFull();

        ArrayList<String> labels = new ArrayList<>();
        ArrayList<Integer> values = new ArrayList<>();
        for (var as : acquiredSkills) {
            labels.add(as.getSkill().getName());
            values.add(as.getLevel());
        }
        JsonArray labelsJsonArray = toJsonArray(labels);
        JsonArray valuesJsonArray = toJsonArray(values);

        if (useChartJs) {
            Div chartJsContainer = new Div();
            chartJsContainer.setId("profile-chartjs");
            chartJsContainer.setWidthFull();
            chartJsContainer.setHeight("320px");
            chartJsContainer.getElement().executeJs(
                "globalThis.skillia.renderSkillsBarChartChartJs($0, $1, $2);",
                "profile-chartjs", labelsJsonArray, valuesJsonArray
            );
            wrapper.add(new H5("Chart.js"), chartJsContainer);
        }

        if (useEcharts) {
            Div echartsContainer = new Div();
            echartsContainer.setId("profile-echarts");
            echartsContainer.setWidthFull();
            echartsContainer.setHeight("320px");
            echartsContainer.getElement().executeJs(
                "globalThis.skillia.renderSkillsBarChartEcharts($0, $1, $2);",
                "profile-echarts", labelsJsonArray, valuesJsonArray
            );
            wrapper.add(new H5("Apache ECharts"), echartsContainer);
        }
    }
}
