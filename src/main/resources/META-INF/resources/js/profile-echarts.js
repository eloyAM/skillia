console.debug("Loading ECharts module");


function cssVarOr(name, fallback) {
    return cssVar(name) || fallback;
}

function cssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
}

function buildVaadinEchartsTheme() {
    // Vaadin Lumo tokens (fallbacks included)
    const bgBase = cssVarOr("--lumo-base-color", "#ffffff");
    const bgContrast = cssVarOr("--lumo-contrast-5pct", "rgba(0,0,0,0.05)");
    const textPrimary = cssVarOr("--lumo-body-text-color", "#1f2937");
    const textSecondary = cssVarOr("--lumo-secondary-text-color", "#6b7280");
    const border = cssVarOr("--lumo-contrast-20pct", "rgba(0,0,0,0.2)");

    const primary = cssVarOr("--lumo-primary-color", "#2563eb");
    const success = cssVarOr("--lumo-success-color", "#16a34a");
    const warning = cssVarOr("--lumo-warning-color", "#d97706");
    const error = cssVarOr("--lumo-error-color", "#dc2626");

    // Extra palette slots (secondary/tertiary style)
    const primary50 = cssVarOr("--lumo-primary-color-50pct", "rgba(37,99,235,0.5)");
    const primary10 = cssVarOr("--lumo-primary-color-10pct", "rgba(37,99,235,0.1)");

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
                borderRadius: [6, 6, 6, 6]
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

function remToPx(rem) {
    if (typeof rem !== 'number' || Number.isNaN(rem)) {
        throw new Error("Invalid input: rem must be a number");
    }
    const rootFontSize = Number.parseFloat(getComputedStyle(document.documentElement).fontSize);
    return rem * rootFontSize;
}

function parseRemString(remString) {
    if (typeof remString !== 'string' || !remString.endsWith('rem')) {
        throw new Error("Invalid rem string");
    }
    const remValue = Number.parseFloat(remString);
    return remToPx(remValue);
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

globalThis.skillia.renderSkillsBulletChartForProfile = function (
    containerId,
    skillNames,
    skillLevels,
    minLevels,
    averageLevels,
    maxLevels,
    recordCounts
) {
    console.debug("Rendering ECharts bullet chart", {
        containerId,
        skillNames,
        skillLevels,
        minLevels,
        averageLevels,
        maxLevels,
        recordCounts
    });
    const host = document.getElementById(containerId);
    if (!host) {
        console.error("Chart container not found:", containerId);
        return;
    }

    const chart = echarts.init(host);

    const cssVarNames = {
        lumoPrimaryColor: '--lumo-primary-color',
        lumoShade10Pct: '--lumo-shade-10pct',
        lumoBodyTextColor: '--lumo-body-text-color',
        lumoSecondaryTextColor: '--lumo-secondary-text-color',
        lumoFontFamily: '--lumo-font-family',
        lumoFontSizeS: '--lumo-font-size-s',
        lumoLineHeightS: '--lumo-line-height-s',
        lumoFontSizeXS: '--lumo-font-size-xs',
        lumoLineHeightXS: '--lumo-line-height-xs'
    };

    const {
        lumoPrimaryColor,
        lumoShade10Pct,
        lumoBodyTextColor,
        lumoSecondaryTextColor,
        lumoFontFamily,
        lumoFontSizeS,
        lumoLineHeightS,
        lumoFontSizeXS,
        lumoLineHeightXS
    } = Object.entries(cssVarNames).reduce((acc, [key, cssVarOr]) => {
        acc[key] = cssVar(cssVarOr);
        return acc;
    }, {});

    const minColor = '#5db5ee';
    const levelColor = lumoPrimaryColor;
    const avgColor = '#189bf3';
    const maxColor = lumoShade10Pct;
    const textColor = lumoBodyTextColor;
    const textSecondary = lumoSecondaryTextColor;
    const fontSize = parseRemString(lumoFontSizeS || "0.875rem");
    const fontLineHeight = Number.parseInt(lumoLineHeightS || 1.375) * fontSize;
    const fontSizeSmaller = parseRemString(lumoFontSizeXS || "0.8125rem");
    const fontLineHeightSmaller = Number.parseInt(lumoLineHeightXS || 1.25) * fontSizeSmaller;

    const option = {
        tooltip: {
            trigger: 'axis',
            axisPointer: {type: 'shadow'},
            extraCssText: 'max-width: 250px; white-space: normal; word-break: break-word;'
        },
        legend: {
            data: ['Person Level', 'Min', 'Average', 'Max'],
            textStyle: {color: textColor}
        },
        xAxis: {
            type: 'value',
            max: 5,
            splitLine: {show: false},
            axisLabel: {
                color: textSecondary,
                textStyle: {
                    fontFamily: lumoFontFamily,
                    lineHeight: fontLineHeightSmaller,
                    fontSize: fontSizeSmaller
                }
            }
        },
        yAxis: {
            type: 'category',
            data: skillNames,
            axisTick: {show: false},
            axisLabel: {
                width: 60, // Wrap if greater than this
                overflow: 'break',
                interval: 0, // Prevent skipping labels
                textStyle: {
                    fontFamily: lumoFontFamily,
                    lineHeight: fontLineHeight,
                    fontSize: fontSize
                },
                color: textSecondary
            }
        },
        grid: {
            left: 10,
            right: 30,
            top: 30,
            bottom: 10,
            containLabel: true
        },
        dataZoom: [
            {
                type: 'slider',
                show: true,
                yAxisIndex: 0,
                // Window size and initial position
                start: 100,
                end: 75,
                // Margin and size
                right: 10,
                width: 10
            },
            {
                // Scroll naturally inside the chart area (instead of moving the slider)
                type: 'inside',
                yAxisIndex: 0,
                zoomOnMouseWheel: 'shift',  // Hold shift button to zoom
                moveOnMouseWheel: true
            }
        ],
        series: [
            {
                name: 'Person Level',
                type: 'bar',
                z: 2,
                barWidth: '30%',
                barMinWidth: 10,
                barMaxWidth: 15,
                itemStyle: {color: levelColor},
                data: skillLevels
            },
            {
                name: 'Min',
                type: 'pictorialBar',
                symbol: 'rect',
                symbolSize: [4, '100%'],
                symbolPosition: 'end',
                z: 3,
                barMinWidth: 10,
                barMaxWidth: 15,
                itemStyle: {color: minColor},
                data: minLevels
            },
            {
                name: 'Average',
                type: 'pictorialBar',
                symbol: 'rect',
                symbolSize: [4, '100%'],
                symbolPosition: 'end',
                z: 3,
                barMinWidth: 10,
                barMaxWidth: 15,
                itemStyle: {color: avgColor},
                data: averageLevels,
                tooltip: {
                    valueFormatter: function (value, dataIndex) {
                        const v = value == null ? value : Number(value).toFixed(2);
                        return `Over ${recordCounts?.[dataIndex] ?? 'N/A'} ratings: ${v}`;
                    }
                }
            },
            {
                name: 'Max',
                type: 'bar',
                barGap: '-100%',
                z: 1,
                barWidth: '60%',
                barMinWidth: 10,
                barMaxWidth: 15,
                itemStyle: {color: maxColor},
                data: maxLevels
            }
        ]
    };

    chart.setOption(option);

    new ResizeObserver(() => {
        chart.resize();
    }).observe(host);
};