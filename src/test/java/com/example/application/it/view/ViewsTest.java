package com.example.application.it.view;

import com.example.application.it.view.testutils.LoginUtility;
import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.html5.LocalStorage;
import org.openqa.selenium.html5.WebStorage;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;

import java.util.List;

import static java.time.Duration.ofSeconds;
import static org.assertj.core.api.Assertions.assertThat;
import static org.openqa.selenium.support.ui.ExpectedConditions.attributeToBe;
import static org.openqa.selenium.support.ui.ExpectedConditions.titleIs;

@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_CLASS)
// Reset the context before running the tests - sometimes the authentication context was not correctly initialized
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ViewsTest {
    private static final Logger logger = LoggerFactory.getLogger(ViewsTest.class);
    private static final String MAIN_USERNAME = "hugo.reyes";
    private WebDriver driver;
    private final String loginUrl;
    private final String homeUrl;

    @Autowired
    public ViewsTest(
        @Value("${local.server.port}") int localServerPort
    ) {
        assertThat(localServerPort).isNotZero();
        String baseUrl = "http://localhost:" + localServerPort;
        this.loginUrl = baseUrl + "/login";
        this.homeUrl = baseUrl + "/";
        logger.info("Using baseUrl '{}'", baseUrl);
    }

    @BeforeEach
    void setUp(
        @Value("${webdriver.headless}") String isHeadless,
        @Value("${webdriver.chrome.binary}") String chromeBinary
    ) {
        ChromeOptions chromeOptions = new ChromeOptions();
        if (Boolean.parseBoolean(isHeadless)) {
            String headlessArg = "--headless=new";
            chromeOptions.addArguments(headlessArg);
            logger.info("Headless arguments added {}", headlessArg);
        }
        if (chromeBinary != null && !chromeBinary.trim().isEmpty()) {
            chromeOptions.setBinary(chromeBinary);
            logger.info("Using specified chrome binary {}", chromeBinary);
        }
        WebDriverManager.chromiumdriver().setup();
        driver = new ChromeDriver(chromeOptions);
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
        usernameInputField.sendKeys(MAIN_USERNAME);
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
    void hasHeaderElements() {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        WebElement appHeader = driver.findElement(By.className("app-header"));

        // Check the app banner
        assertThat(appHeader.findElement(By.tagName("h1")))
            .extracting(WebElement::getText)
            .isEqualTo("Skillia");

        // Check that some elements are there
        appHeader.findElements(By.tagName("vaadin-drawer-toggle"));
        appHeader.findElement(By.id("app-theme-switcher"));
        appHeader.findElement(By.id("app-profile-element"));
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
        driver.findElement(By.cssSelector(".app-header #app-profile-element vaadin-menu-bar-button"))
            .click();   // Click the profile button to make the logout one available
        driver.findElement(By.id("app-logout-button"))
            .click();

        waitUntilTitleIs("Login");

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

    @Test
    void canNavigateToUserProfile() {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        // Click the profile button to make the profile navigation available
        driver.findElement(By.cssSelector(".app-header #app-profile-element vaadin-menu-bar-button"))
            .click();

        // Click the profile/user profile button
        WebElement profileButton = driver.findElement(By.id("app-my-profile-button"));
        profileButton.click();

        // Wait for navigation to complete
        new WebDriverWait(driver, ofSeconds(5), ofSeconds(1))
            .withMessage("Expecting to get redirected to the profile view")
            .until(d -> d.getCurrentUrl().contains("/profile"));

        assertThat(driver.getCurrentUrl()).contains("/profile/" + MAIN_USERNAME);
    }

    // Exclude links that are already checked by some other tests
    @ParameterizedTest
    @EnumSource(value = DrawerLink.class, names = {"MY_PROFILE", "SKILLS_MANAGEMENT"}, mode = EnumSource.Mode.EXCLUDE)
    void canNavigateToDrawerLinks(DrawerLink drawerLink) {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        // Click the drawer link
        WebElement drawerLinkElement = driver.findElement(By.id(drawerLink.htmlElementId));
        drawerLinkElement.click();

        // Wait for navigation to complete
        new WebDriverWait(driver, ofSeconds(5), ofSeconds(1))
            .withMessage("Expecting to get redirected to the correct view")
            .until(d -> d.getCurrentUrl().contains(drawerLink.targetRoute));

        // Verify the URL
        assertThat(driver.getCurrentUrl()).contains(drawerLink.targetRoute);
    }

    // As long as the tabs are not lazy-loaded,
    // there's no coverage difference between clicking each different tab or not
    @Test
    void canViewAllTabsInSkillsManagement() {
        LoginUtility.doLogin(driver, loginUrl);
        getAndWaitUntilTitleIs(homeUrl, "Skillia");

        // Navigate to the Skills Management view
        WebElement skillsManagementLink = driver.findElement(By.id(DrawerLink.SKILLS_MANAGEMENT.htmlElementId));
        skillsManagementLink.click();
        new WebDriverWait(driver, ofSeconds(5), ofSeconds(1))
            .until(d -> d.getCurrentUrl().contains(DrawerLink.SKILLS_MANAGEMENT.targetRoute));

        // Locate the vaadin-tab elements
        List<WebElement> tabs = driver.findElements(By.cssSelector("vaadin-tabs vaadin-tab"));

        // Verify that there are three tabs
        assertThat(tabs).hasSize(3);

        // Iterate through each tab and verify it can be selected
        for (WebElement tab : tabs) {
            // Click the tab
            tab.click();
            new WebDriverWait(driver, ofSeconds(2))
                .until(attributeToBe(tab, "selected", "true"));

            // Get the tab content
            String tabId = tab.getAttribute("id");
            WebElement tabContent = new WebDriverWait(driver, ofSeconds(2))
                .until(d -> d.findElement(By.cssSelector(
                    "vaadin-tabsheet vaadin-vertical-layout[tab='%s']".formatted(tabId))));
            assertThat(tabContent.isDisplayed()).isTrue();

            // Click the button to add a new object, displaying a modal dialog
            WebElement addObjectButton = tabContent.findElement(By.cssSelector("vaadin-button[theme~='primary']"));
            addObjectButton.click();
            WebElement dialog = new WebDriverWait(driver, ofSeconds(2))
                .until(d -> d.findElement(By.tagName("vaadin-dialog-overlay")));
            assertThat(dialog.isDisplayed()).isTrue();

            // Close the dialog
            WebElement closeButton = dialog.findElement(By.className("cancel-button"));
            closeButton.click();
            new WebDriverWait(driver, ofSeconds(2))
                .until(ExpectedConditions.invisibilityOf(dialog));
        }
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

    private enum ColorScheme {
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

    private enum DrawerLink {
        MY_PROFILE("my-profile", "/profile/%s"),
        SKILLS_MATRIX("skills-matrix", "/skillsmatrix"),
        SKILLS_ASSIGNMENT("skills-assignment", "/skillsassignment"),
        SKILLS_MANAGEMENT("skills-management", "/skillsmanagement"),
        DEPARTMENTS("departments", "/departments");

        final String htmlElementId;
        final String targetRoute;

        DrawerLink(String htmlElementIdSuffix, String targetRoute) {
            this.htmlElementId = "drawer-link-" + htmlElementIdSuffix;
            this.targetRoute = targetRoute;
        }
    }
}
