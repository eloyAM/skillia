console.debug("Loading ECharts module");


function cssVar(name, fallback) {
    const v = getComputedStyle(document.documentElement).getPropertyValue(name).trim();
    return v || fallback;
}

function buildVaadinEchartsTheme() {
    // Vaadin Lumo tokens (fallbacks included)
    const bgBase = cssVar("--lumo-base-color", "#ffffff");
    const bgContrast = cssVar("--lumo-contrast-5pct", "rgba(0,0,0,0.05)");
    const textPrimary = cssVar("--lumo-body-text-color", "#1f2937");
    const textSecondary = cssVar("--lumo-secondary-text-color", "#6b7280");
    const border = cssVar("--lumo-contrast-20pct", "rgba(0,0,0,0.2)");

    const primary = cssVar("--lumo-primary-color", "#2563eb");
    const success = cssVar("--lumo-success-color", "#16a34a");
    const warning = cssVar("--lumo-warning-color", "#d97706");
    const error = cssVar("--lumo-error-color", "#dc2626");

    // Extra palette slots (secondary/tertiary style)
    const primary50 = cssVar("--lumo-primary-color-50pct", "rgba(37,99,235,0.5)");
    const primary10 = cssVar("--lumo-primary-color-10pct", "rgba(37,99,235,0.1)");

    return {
        color: [primary, success, warning, error, primary50, "#7c3aed", "#0ea5e9"],
        backgroundColor: bgBase,

        textStyle: {color: textPrimary},

        title: {
            textStyle: {color: textPrimary, fontWeight: 600},
            subtextStyle: {color: textSecondary}
        },

        legend: {
            textStyle: {color: textSecondary}
        },

        tooltip: {
            backgroundColor: bgBase,
            borderColor: border,
            borderWidth: 1,
            textStyle: {color: textPrimary}
        },

        grid: {
            containLabel: true
        },

        categoryAxis: {
            axisLine: {lineStyle: {color: border}},
            axisTick: {lineStyle: {color: border}},
            axisLabel: {color: textSecondary},
            splitLine: {show: false}
        },

        valueAxis: {
            axisLine: {lineStyle: {color: border}},
            axisTick: {lineStyle: {color: border}},
            axisLabel: {color: textSecondary},
            splitLine: {lineStyle: {color: bgContrast}}
        },

        // Optional defaults for bars/lines
        bar: {
            itemStyle: {
                borderRadius: [6, 6, 0, 0]
            }
        },
        line: {
            smooth: true,
            symbol: "circle",
            symbolSize: 7
        },

        // Emphasis style close to Vaadin primary surface usage
        emphasis: {
            itemStyle: {
                shadowBlur: 10,
                shadowColor: primary10
            }
        }
    };
}

function registerOrUpdateVaadinTheme() {
    // Re-register using the same theme name whenever tokens might have changed
    echarts.registerTheme("vaadin-lumo", buildVaadinEchartsTheme());
}


globalThis.skillia = globalThis.skillia || {};
globalThis.skillia.renderSkillsBarChartEcharts = function renderSkillsBarChartEcharts(containerId, labels, values) {
    console.debug("Rendering ECharts bar chart", {containerId, labels, values});

    const host = document.getElementById(containerId);
    if (!host || typeof echarts === "undefined") return;

    function initChart() {
        const current = echarts.getInstanceByDom(host);
        if (current) current.dispose();
        registerOrUpdateVaadinTheme();
        const chart = echarts.getInstanceByDom(host) || echarts.init(host, 'vaadin-lumo');
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
        return chart;
    }

    let chart = initChart();

    // This option works better than listening to window resize
    const resizeObserver = new ResizeObserver(() => {
        chart.resize();
    });
    resizeObserver.observe(host);

    // Rebuild if we switch the theme
    const mo = new MutationObserver(() => {
        chart = initChart();
    });
    mo.observe(document.documentElement, {
        attributes: true,
        attributeFilter: ["theme", "class"]
    });

    // Cleanup for re-navigation
    host._skilliaCleanup?.();
    host._skilliaCleanup = () => {
        resizeObserver.disconnect();
        mo.disconnect();
    }
}