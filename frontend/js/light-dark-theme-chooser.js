(function (document, console, window, localStorage) {
    "use strict";
    function changeThemeWithThemeName(theme) {
        document.documentElement.setAttribute("theme", theme);
    }
    function changeThemeIfDark(isDark) {
        var theme = isDark ? "dark" : "light";
        changeThemeWithThemeName(theme);
    }
    function changeThemeIfMediaMatches(e) {
        changeThemeIfDark(e.matches);
    }
    function mainFn() {
        var localStorageTheme = localStorage.getItem("app-theme");
        if (localStorageTheme) {
            changeThemeWithThemeName(localStorageTheme);
        }
        else {
            // If the user haven't changed the theme manually, follow the device theme
            var matchMedia = window.matchMedia("(prefers-color-scheme: dark)");
            changeThemeIfMediaMatches(matchMedia);
            matchMedia.addEventListener("change", changeThemeIfMediaMatches);
        }
    }
    if (document.readyState === "complete" || document.readyState === "loaded") {
        mainFn();
    }
    else {
        document.addEventListener("DOMContentLoaded", mainFn);
    }
}(document, console, window, localStorage));
