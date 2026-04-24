package com.example.application.view.page;

import com.example.application.view.utils.MainLayout;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import jakarta.annotation.security.PermitAll;

@PermitAll
@Route(layout = MainLayout.class, value = "charts/example01")
@PageTitle("Charts Demo")
public class ChartExample01View extends VerticalLayout {

    public ChartExample01View() {
        setSizeFull();
        add(chartJsExample());
        add(echartsExample());
    }

    private Div chartJsExample() {
        Div chartContainer = new Div();
        chartContainer.setId("chartjs-demo");
        chartContainer.setWidth("900px");
        chartContainer.setHeight("420px");

        // Chart.js uses echartsExample canvas element
        chartContainer.getElement().setProperty("innerHTML", "<canvas id='myChart'></canvas>");

        // Render after attach
        chartContainer.getElement().executeJs(
            """
                const canvas = document.getElementById('myChart');
                if (!canvas || typeof Chart === 'undefined') return;
                
                const existing = Chart.getChart(canvas);
                if (existing) existing.destroy();
                
                new Chart(canvas, {
                  type: 'bar',
                  data: {
                    labels: ['Java', 'Kotlin', 'TypeScript', 'Python', 'Go'],
                    datasets: [{
                      label: 'Skill votes',
                      data: [12, 8, 14, 10, 6]
                    }]
                  },
                  options: {
                    responsive: true,
                    maintainAspectRatio: false
                  }
                });
                """
        );
        return chartContainer;
    }

    private Div echartsExample() {
        Div chartContainer = new Div();
        chartContainer.setId("echarts-demo");
        chartContainer.setWidth("900px");
        chartContainer.setHeight("420px");
        add(chartContainer);

        chartContainer.getElement().executeJs(
            """
                const el = document.getElementById('echarts-demo');
                if (!el || typeof echarts === 'undefined') return;
                
                const chart = echarts.init(el);
                
                chart.setOption({
                  title: { text: 'Monthly Registrations' },
                  tooltip: {},
                  xAxis: {
                    type: 'category',
                    data: ['Jan', 'Feb', 'Mar', 'Apr', 'May', 'Jun']
                  },
                  yAxis: { type: 'value' },
                  series: [{
                    type: 'line',
                    smooth: true,
                    data: [23, 45, 31, 62, 54, 77]
                  }]
                });
                
                window.addEventListener('resize', () => chart.resize());
                """
        );
        return chartContainer;
    }
}