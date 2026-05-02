globalThis.skillia = globalThis.skillia || {};
globalThis.skillia.renderSkillsBulletChartExample = function (containerId, indexOption = 0) {
    const host = document.getElementById(containerId);
    if (!host) {
        console.error("Chart container not found:", containerId);
        return;
    }

    const chart = echarts.init(host);


    const skillNames = ['Java', 'Spring Boot', 'Vaadin', 'SQL', 'JavaScript'];
    const skillLevels = [4, 3, 5, 3, 2];
    const minLevels = [1, 1, 2, 3, 2];
    const meanLevels = [3.2, 3.5, 4.1, 4, 3.8];
    const maxLevels = [5, 4, 5, 5, 4];

    const lumoPrimaryColor = getComputedStyle(document.documentElement).getPropertyValue('--lumo-primary-color').trim();
    const lumoShade10Pct = getComputedStyle(document.documentElement).getPropertyValue('--lumo-shade-10pct').trim();
    const lumoBodyTextColor = getComputedStyle(document.documentElement).getPropertyValue('--lumo-body-text-color').trim();

    const symbolSize = 10;

    const symbolSizeRect = [3, 10];

    const minColor = '#5db5ee';
    // const levelColor = '#36A2EB';
    const levelColor = lumoPrimaryColor;
    const avgColor = '#189bf3';
    const maxColor = lumoShade10Pct;
    const textColor = lumoBodyTextColor;

    let options = [
        //
        // Option -1 - bullet
        //
        {
            tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
            legend: {
                data: ['Person Level', 'Max', 'Average']
            },
            xAxis: {type: 'value', max: 5, splitLine: {show: false}},
            yAxis: {
                type: 'category',
                data: skillNames,
                axisTick: {show: false}
            },
            series: [
                {
                    name: 'Max',
                    type: 'bar',
                    barGap: '-100%',
                    barWidth: '60%',
                    itemStyle: {color: '#e0e0e0'},
                    data: maxLevels
                },
                {
                    name: 'Person Level',
                    type: 'bar',
                    z: 2,
                    barWidth: '30%',
                    itemStyle: {color: '#36A2EB'},
                    data: skillLevels
                },
                {
                    name: 'Average',
                    type: 'scatter',
                    symbol: 'rect',
                    symbolSize: [4, 30],
                    z: 3,
                    itemStyle: {color: '#FF6384'},
                    data: meanLevels
                }
            ]
        },
        //
        // Option 0 - bullet modified
        //
        {
            tooltip: {trigger: 'axis', axisPointer: {type: 'shadow'}},
            legend: {
                data: ['Person Level', 'Min', 'Average', 'Max'],
                textStyle: {color: textColor}
            },
            xAxis: {type: 'value', max: 5, splitLine: {show: false}},
            yAxis: {
                type: 'category',
                data: skillNames,
                axisTick: {show: false}
            },
            series: [
                {
                    name: 'Person Level',
                    type: 'bar',
                    z: 4,
                    barWidth: '30%',
                    itemStyle: {color: levelColor},
                    data: skillLevels
                },
                {
                    name: 'Min',
                    type: 'scatter',
                    symbol: 'rect',
                    symbolSize: [4, 30],
                    z: 2,
                    itemStyle: {color: minColor},
                    data: minLevels
                },
                {
                    name: 'Average',
                    type: 'scatter',
                    z: 3,
                    symbol: 'rect',
                    symbolSize: [4, 30],
                    itemStyle: {color: avgColor},
                    data: meanLevels
                },
                {
                    name: 'Max',
                    type: 'bar',
                    barGap: '-100%',
                    z: 1,
                    barWidth: '60%',
                    itemStyle: {color: maxColor},
                    data: maxLevels
                },
            ]
        },
        //
        // Option 1 - layered with toggleable layers
        //
        {
            // title: {text: 'Layered Rating Chart (Toggleable Layers)'},
            tooltip: {trigger: 'axis'},
            legend: {
                data: ['Rating', 'Min', 'Mean', 'Max']
            },

            xAxis: {type: 'value', max: 5},
            yAxis: {type: 'category', data: skillNames},

            series: [
                // Base bar layer
                {
                    name: 'Rating',
                    type: 'bar',
                    barWidth: '50%',
                    itemStyle: {color: lumoPrimaryColor},
                    data: skillLevels
                },
                // Min marker layer
                {
                    name: 'Min',
                    type: 'scatter',
                    // symbol: 'triangle',
                    // symbolRotate: 180,
                    symbolSize: symbolSize,
                    itemStyle: {color: '#E74C3C'},
                    data: minLevels
                },
                // Mean marker layer
                {
                    name: 'Mean',
                    type: 'scatter',
                    // symbol: 'diamond',
                    symbolSize: symbolSize,
                    itemStyle: {color: '#F1C40F'},
                    data: meanLevels
                },
                // Max marker layer
                {
                    name: 'Max',
                    type: 'scatter',
                    // symbol: 'circle',
                    symbolSize: symbolSize,
                    itemStyle: {color: '#2ECC71'},
                    data: maxLevels
                }
            ]
        },
        //
        // Option 2 - layered with toggleable layers - variation
        //
        {
            // title: {text: 'Layered Rating Chart (Toggleable Layers)'},
            tooltip: {trigger: 'axis'},
            legend: {
                data: ['Rating', 'Min', 'Average', 'Max']
            },

            xAxis: {type: 'value', max: 5},
            yAxis: {type: 'category', data: skillNames},

            series: [
                // Base bar layer
                {
                    name: 'Rating',
                    type: 'bar',
                    barWidth: '60%',
                    itemStyle: {color: lumoPrimaryColor},
                    data: skillLevels
                },
                // Min marker layer
                {
                    name: 'Min',
                    type: 'scatter',
                    // symbol: 'triangle',
                    // symbolRotate: 180,
                    // symbolSize: symbolSize,
                    symbol: 'rect',
                    symbolSize: symbolSizeRect,
                    itemStyle: {color: '#E74C3C'},
                    data: minLevels
                },
                // Mean marker layer
                {
                    name: 'Average',
                    type: 'scatter',
                    // symbol: 'diamond',
                    // symbolSize: symbolSize,
                    // symbol: 'rect',
                    // symbolSize: symbolSizeRect,
                    itemStyle: {color: '#F1C40F'},
                    data: meanLevels
                },
                // Max marker layer
                {
                    name: 'Max',
                    type: 'scatter',
                    // symbol: 'circle',
                    // symbolSize: symbolSize,
                    symbol: 'rect',
                    symbolSize: symbolSizeRect,
                    itemStyle: {color: '#2ECC71'},
                    data: maxLevels
                }
            ]
        }
    ];

    chart.setOption(options[indexOption]);

    new ResizeObserver(() => {
        chart.resize();
    }).observe(host);
};