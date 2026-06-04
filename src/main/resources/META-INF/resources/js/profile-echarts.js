globalThis.skillia = globalThis.skillia || {};

function cssVar(name) {
    return getComputedStyle(document.documentElement).getPropertyValue(name).trim();
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

    function readCssVars() {
        return Object.entries(cssVarNames).reduce((acc, [key, cssVarOr]) => {
            acc[key] = cssVar(cssVarOr);
            return acc;
        }, {});
    }

    function buildOption() {
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
        } = readCssVars();

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

        return {
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
    }

    chart.setOption(buildOption());

    // Re-apply chart options with the updated CSS values after switching the theme
    new MutationObserver(() => {
        try {
            chart.setOption(buildOption(), true); // true -> override existing
            chart.resize();
        } catch (err) {
            console.error('Failed to update chart after theme change', err);
        }
    }).observe(document.documentElement, {
        attributes: true,
        attributeFilter: ['theme']
    });

    new ResizeObserver(() => {
        chart.resize();
    }).observe(host);
};