package com.example.application.it.view.testutils;

import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class LoginUtility {
    public static final String JWT_HEADER_AND_PAYLOAD_COOKIE_NAME = "jwt.headerAndPayload";
    public static final String JWT_SIGNATURE_COOKIE_NAME = "jwt.signature";

    private LoginUtility() {
    }

    public static void doLogin(WebDriver driver, String loginUrl) {
        doLogin(driver, loginUrl, "hugo.reyes", "1234");
    }

    public static void doLogin(WebDriver driver, String loginUrl, String username, String password) {
        // Access and wait for the page to load
        driver.get(loginUrl);
        new WebDriverWait(driver, Duration.ofSeconds(5), Duration.ofSeconds(1))
                .until(ExpectedConditions.titleIs("Login"));

        // Get the login form elements
        WebElement loginForm = driver.findElement(By.tagName("vaadin-login-form"));
        WebElement usernameInputField = loginForm.findElement(By.cssSelector("input[name='username'"));
        WebElement passwordInputField = loginForm.findElement(By.cssSelector("input[name='password'"));
        WebElement submitButton = loginForm.findElement(By.cssSelector("vaadin-button[slot='submit'"));

        // Fill the login form and submit
        usernameInputField.click();
        usernameInputField.sendKeys(username);
        passwordInputField.click();
        passwordInputField.sendKeys(password);
        submitButton.click();
    }
}
