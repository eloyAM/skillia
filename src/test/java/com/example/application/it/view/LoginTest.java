package com.example.application.it.view;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginTest.class);
    public static final String DARK = "dark";
    public static final String LIGHT = "light";
    private WebDriver driver;
    private final String baseUrl;
    private final String loginUrl;
    private final String homeUrl;

    @Autowired
    public LoginTest(
            @Value("${local.server.port}") int localServerPort
    ) {
        assertThat(localServerPort).isNotZero();
        this.baseUrl = "http://localhost:" + localServerPort;
        this.loginUrl = baseUrl + "/login";
        this.homeUrl = baseUrl + "/";
        logger.info("Using baseUrl '" + baseUrl + "'");
    }

    @BeforeEach
    void setUp(
    ) {
        driver = new ChromeDriver();
        WebDriverManager.chromedriver().setup();
    }

    @AfterEach
    void tearDown() {
        driver.quit();
    }

    @Test
    void canLogIn() {
        // Access the login page (and wait for the content to be rendered)
        getAndWaitUntilTitleIs(loginUrl, "Login");

        // Verify that the page title is correct
        assertThat(driver.getTitle()).isEqualTo("Login");

        // Get the login form elements
        WebElement loginForm = driver.findElement(By.tagName("vaadin-login-form"));
        WebElement usernameInputField = loginForm.findElement(By.cssSelector("input[name='username'"));
        WebElement passwordInputField = loginForm.findElement(By.cssSelector("input[name='password'"));
        WebElement submitButton = loginForm.findElement(By.cssSelector("vaadin-button[slot='submit'"));

        // Fill the login form and submit
        usernameInputField.click();
        usernameInputField.sendKeys("hugo.reyes");
        passwordInputField.click();
        passwordInputField.sendKeys("1234");
        submitButton.click();

        // Wait for the page to load
        waitUntilTitleIs("Skillia");

        // Check that we are now on the home page
        assertThat(driver.getCurrentUrl()).isEqualTo(homeUrl);

        // Check some cookies

        Cookie jwtHeaderAndPlayloadCookie = driver.manage()
                .getCookieNamed(LoginUtility.JWT_HEADER_AND_PAYLOAD_COOKIE_NAME);
        assertThat(jwtHeaderAndPlayloadCookie.isHttpOnly()).isFalse();
        assertThat(jwtHeaderAndPlayloadCookie.getValue()).matches("^[A-Za-z0-9_-]{2,}\\.[A-Za-z0-9_-]{2,}$");

        Cookie jwtSignatureCookie = driver.manage()
                .getCookieNamed(LoginUtility.JWT_SIGNATURE_COOKIE_NAME);
        assertThat(jwtSignatureCookie.isHttpOnly()).isTrue();
        assertThat(jwtSignatureCookie.getValue()).matches("^[A-Za-z0-9_-]{2,}$");
    }

    @Test
    void headerIsPresent() {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        WebElement appHeader = driver.findElement(By.className("app-header"));

        // Check the app banner
        assertThat(appHeader.findElement(By.tagName("h1")))
                .extracting(WebElement::getText)
                .isEqualTo("Skillia");

        // Check that some elements are there
        appHeader.findElement(By.id("app-theme-switcher"));
        appHeader.findElement(By.id("app-logout-button"));
        appHeader.findElements(By.tagName("vaadin-drawer-toggle"));
    }

    @Test
    void canLogOut() {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        // Precondition for later assertion
        WebDriver.Options manage = driver.manage();
        assertThat(manage.getCookieNamed(LoginUtility.JWT_HEADER_AND_PAYLOAD_COOKIE_NAME)
                .getValue())
                .isNotEmpty();
        assertThat(manage.getCookieNamed(LoginUtility.JWT_SIGNATURE_COOKIE_NAME)
                .getValue())
                .isNotEmpty();

        // Click the logout button
        WebElement appHeader = driver.findElement(By.className("app-header"));
        appHeader.findElement(By.id("app-logout-button"))
                .click();

        // Expect the auth cookies to be cleaned
        assertThat(driver.manage().getCookies())
                .filteredOn(c -> c.getName().equals(LoginUtility.JWT_HEADER_AND_PAYLOAD_COOKIE_NAME)
                        || c.getName().equals(LoginUtility.JWT_SIGNATURE_COOKIE_NAME))
                .isEmpty();
    }


    @Test
    void canSwitchBetweenDarkAndLightTheme() {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        LocalStorage localStorage = ((WebStorage) driver).getLocalStorage();

        // Theme matches the preferred color scheme of the device (light/dark)

        JavascriptExecutor js = (JavascriptExecutor) driver;
        Boolean prefersDarkTheme = (Boolean) js.executeScript(
                "return window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches;");

        ColorScheme colorScheme = prefersDarkTheme ? ColorScheme.DARK : ColorScheme.LIGHT;

        assertThat(getHtmlRootElement().getAttribute("theme"))
                .isEqualTo(colorScheme.value);
        // Local storage property is not set initially
        assertThat(localStorage.getItem("app-theme"))
                .isNull();

        // Switch to the second theme
        clickSwitchTheme();
        colorScheme = colorScheme.toggle();
        new WebDriverWait(driver, ofSeconds(2))
                .until(attributeToBe(getHtmlRootElement(), "theme", colorScheme.value));
        assertThat(localStorage.getItem("app-theme")).isEqualTo(colorScheme.value);

        // And back to the preferred one
        clickSwitchTheme();
        colorScheme = colorScheme.toggle();
        new WebDriverWait(driver, ofSeconds(2))
                .until(attributeToBe(getHtmlRootElement(), "theme", colorScheme.value));
        assertThat(localStorage.getItem("app-theme")).isEqualTo(colorScheme.value);
    }

    // Helpers

    private void waitUntilTitleIs(String title) {
        new WebDriverWait(driver, ofSeconds(5), ofSeconds(1)).until(titleIs(title));
    }

    private void getAndWaitUntilTitleIs(String url, String title) {
        driver.get(url);
        waitUntilTitleIs(title);
    }

    private WebElement getHtmlRootElement() {
        return driver.findElement(By.tagName("html"));
    }

    private void clickSwitchTheme() {
        driver.findElement(By.id("app-theme-switcher"))
                .click();
    }

    enum ColorScheme {
        LIGHT("light"),

        DARK("dark");

        final String value;

        ColorScheme(String value) {
            this.value = value;
        }

        ColorScheme toggle() {
            return switch (this) {
                case LIGHT -> DARK;
                case DARK -> LIGHT;
            };
        }
    }

}
