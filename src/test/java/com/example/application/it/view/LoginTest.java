package com.example.application.it.view;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.openqa.selenium.By;
import org.openqa.selenium.Cookie;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.WebTestClient;

import java.time.Duration;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class LoginTest {
    private static final Logger logger = LoggerFactory.getLogger(LoginTest.class);
    private WebDriver driver;
    private final WebTestClient wtc;
    private final String baseUrl;
    private final String loginUrl;
    private final String homeUrl;

    @Autowired
    public LoginTest(
            WebTestClient wtc,
            @Value("${local.server.port}") int localServerPort
    ) {
        this.wtc = wtc;

        assertThat(localServerPort).isNotZero();
        this.baseUrl = "http://localhost:" + localServerPort;
        this.loginUrl = baseUrl + "/login";
        this.homeUrl = baseUrl + "/";
        logger.warn("Using baseUrl '" + baseUrl + "'");
    }

    @BeforeEach
    void setUp(
    ) {
        wtc.get().uri(loginUrl)
                .exchange()
                .expectStatus().isOk()
                .expectHeader().contentTypeCompatibleWith(MediaType.TEXT_HTML)
                .expectBody()
                // Cannot assert the content as it is filled with JS
                .consumeWith(b -> assertThat(b).asString().isNotEmpty())
                .consumeWith(System.out::println);
        driver = new ChromeDriver();
        WebDriverManager.chromedriver().setup();
    }

    @AfterEach
    void tearDown() {
        driver.close();
        driver.quit();
    }

    @Test
    public void testPageLoads() {
        // Access the login page (and wait for the content to be rendered)
        driver.get(loginUrl);
        new WebDriverWait(driver, Duration.ofSeconds(5), Duration.ofSeconds(1))
                .until(ExpectedConditions.titleIs("Login"));

        // Verify that the page title is correct
        assertThat(driver.getTitle()).isEqualTo("Login");

        // Get the login form elements
        WebElement loginForm = driver.findElement(By.tagName("vaadin-login-form"));
        WebElement usernameInputField = loginForm.findElement(By.cssSelector("input[name='username'"));
        WebElement passwordInputField = loginForm.findElement(By.cssSelector("input[name='password'"));
        WebElement submitButton = loginForm.findElement(By.cssSelector("vaadin-button[slot='submit'"));

        // Fill the login form and submit
        usernameInputField.sendKeys("hugo.reyes");
        passwordInputField.sendKeys("1234");
        submitButton.click();

        // Wait for the page to load
        new WebDriverWait(driver, Duration.ofSeconds(5), Duration.ofSeconds(1))
                .until(ExpectedConditions.titleIs("Skillia"));

        // Check that we are now on the home page
        assertThat(driver.getCurrentUrl()).isEqualTo(homeUrl);
        assertThat(driver.getTitle()).isEqualTo("Skillia");

        // Check some cookies

        Cookie jwtHeaderAndPlayloadCookie = driver.manage().getCookieNamed("jwt.headerAndPayload");
        assertThat(jwtHeaderAndPlayloadCookie.isHttpOnly()).isFalse();
        assertThat(jwtHeaderAndPlayloadCookie.getValue()).matches("^[A-Za-z0-9_-]{2,}\\.[A-Za-z0-9_-]{2,}$");

        Cookie jwtSignatureCookie = driver.manage().getCookieNamed("jwt.signature");
        assertThat(jwtSignatureCookie.isHttpOnly()).isTrue();
        assertThat(jwtSignatureCookie.getValue()).matches("^[A-Za-z0-9_-]{2,}$");
    }
}
