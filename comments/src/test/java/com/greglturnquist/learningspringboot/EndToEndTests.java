package com.greglturnquist.learningspringboot;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.openqa.selenium.By;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeDriverService;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.interactions.Actions;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.util.FileCopyUtils;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class EndToEndTests {

    static ChromeDriverService service;
    static ChromeDriver driver;

    private static final String PATH_TO_DRIVER = "C:\\ProgramData\\chocolatey\\bin\\chromedriver.exe";
    @LocalServerPort int port;

    @BeforeAll
    public static void setUp() throws IOException {
        System.setProperty("webdriver.chrome.driver", PATH_TO_DRIVER);
        service = ChromeDriverService.createDefaultService();
        ChromeOptions options = new ChromeOptions();
        options.addArguments("--remote-allow-origins=*");
        driver = new ChromeDriver(service, options);
        Path testResults = Paths.get("build", "test-results");
        if (!Files.exists(testResults)) {
            Files.createDirectory(testResults);
        }
    }

    @AfterAll
    public static void  tearDown() {
        service.stop();
    }

    @Test
    public void homePageShouldWork() throws IOException {
        driver.get("http://localhost:" + port);
        takeScreenshot("homePageShouldWork-1");
        assertThat(driver.getTitle()).isEqualTo("Learning Spring Boot: Spring-a-Gram");

        String pageContent = driver.getPageSource();
        assertThat(pageContent).contains("<a href=\"/images/bazinga.png/raw\">");

        WebElement element = driver.findElement(By.cssSelector("a[href*=\"bazinga.png/raw\"]"));
        Actions actions = new Actions(driver);
        actions.moveToElement(element).click().perform();
        takeScreenshot("homePageShouldWork-2");
        driver.navigate().back();
    }

    private void takeScreenshot(String name) throws IOException {
        FileCopyUtils.copy(driver.getScreenshotAs(OutputType.FILE), new File("build/test-results/TEST-" + name + ".png"));
    }
}
