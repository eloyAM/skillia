package com.example.application.view.components.profile;

import com.example.application.dto.AcquiredSkillDto;
import com.example.application.dto.StatValue;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H5;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import elemental.json.Json;
import elemental.json.JsonArray;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import static com.example.application.view.utils.JsonUtils.setJsonValue;

public class AcquiredSkillsChart extends VerticalLayout {

    public AcquiredSkillsChart(
        boolean useChartJs, boolean useEcharts, List<AcquiredSkillDto> acquiredSkills, Map<Long, StatValue> skillStats
    ) {
        VerticalLayout wrapper = this;
        wrapper.addClassName("user-profile-charts-layout");
        wrapper.setPadding(false);
        wrapper.getStyle().setPaddingBottom("var(--lumo-space-xl)");
        wrapper.setSpacing(true);
        wrapper.setWidthFull();

        JsonArray labelsJsonArray = Json.createArray();
        JsonArray valuesJsonArray = Json.createArray();
        JsonArray minValuesJsonArray = Json.createArray();
        JsonArray maxValuesJsonArray = Json.createArray();
        JsonArray avgValuesJsonArray = Json.createArray();
        JsonArray countValuesJsonArray = Json.createArray();
        int i = 0;
        for (var as : acquiredSkills) {
            setJsonValue(labelsJsonArray, i, as.getSkill().getName());
            setJsonValue(valuesJsonArray, i, as.getLevel());
            Optional<StatValue> statValueOpt = Optional.ofNullable(skillStats.get(as.getSkill().getId()));
            setJsonValue(minValuesJsonArray, i, statValueOpt.map(StatValue::getMin).orElse(null));
            setJsonValue(avgValuesJsonArray, i, statValueOpt.map(StatValue::getAverage).orElse(null));
            setJsonValue(maxValuesJsonArray, i, statValueOpt.map(StatValue::getMax).orElse(null));
            setJsonValue(countValuesJsonArray, i, statValueOpt.map(StatValue::getCount).orElse(null));
            i++;
        }


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

        Div chartContainer = new Div();
        String id = "profile-echarts-bullet";
        chartContainer.setId(id);
        chartContainer.setWidthFull();
        chartContainer.setHeight("350px");
        chartContainer.getElement().executeJs(
            "globalThis.skillia.renderSkillsBulletChartForProfile($0, $1, $2, $3, $4, $5, $6);",
            id,
            labelsJsonArray, valuesJsonArray,
            minValuesJsonArray, avgValuesJsonArray, maxValuesJsonArray, countValuesJsonArray
        );
        wrapper.add(chartContainer);
    }
}
