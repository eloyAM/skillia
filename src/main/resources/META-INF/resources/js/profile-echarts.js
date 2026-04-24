console.debug("Loading ECharts module");

globalThis.skillia = globalThis.skillia || {};
globalThis.skillia.renderSkillsBarChartEcharts = function renderSkillsBarChartEcharts(containerId, labels, values) {
    console.debug("Rendering ECharts bar chart", {containerId, labels, values});

    const host = document.getElementById(containerId);
    if (!host || typeof echarts === "undefined") return;

    const chart = echarts.getInstanceByDom(host) || echarts.init(host);

    const max = 5;

    chart.setOption({
        tooltip: {trigger: "axis"},
        xAxis: {
            type: "category",
            data: labels
        },
        yAxis: {
            type: "value",
            min: 0,
            max
        },
        series: [
            {
                name: "Skill level",
                type: "bar",
                data: values
            }
        ],
        grid: {
            left: 40,
            right: 20,
            top: 20,
            bottom: 60
        }
    });

    // This option works better than listening to window resize
    const resizeObserver = new ResizeObserver(() => {
        chart.resize();
    });
    resizeObserver.observe(host);
}