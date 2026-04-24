console.debug("Loading Chart.js module");

globalThis.skillia = globalThis.skillia || {};
globalThis.skillia.renderSkillsBarChartChartJs = function renderSkillsBarChartChartJs(containerId, labels, values) {
    console.debug("Rendering Chart.js bar chart", {containerId, labels, values});

    const host = document.getElementById(containerId);
    if (!host || typeof Chart === "undefined") return;

    // Recreate canvas to avoid stale Chart instances on Vaadin re-navigation
    host.innerHTML = "<canvas></canvas>";
    const canvas = host.querySelector("canvas");

    const max = 5;

    new Chart(canvas, {
        type: "bar",
        data: {
            labels: labels,
            datasets: [
                {
                    label: "Skill level",
                    data: values
                }
            ]
        },
        options: {
            responsive: true,
            maintainAspectRatio: false,
            scales: {
                y: {
                    min: 0,
                    max
                }
            }
        }
    });
}