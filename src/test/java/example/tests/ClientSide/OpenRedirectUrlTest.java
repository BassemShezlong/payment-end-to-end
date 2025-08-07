package example.tests.ClientSide;

import io.github.bonigarcia.wdm.WebDriverManager;
import org.openqa.selenium.*;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.testng.annotations.Test;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.Duration;

public class OpenRedirectUrlTest {

    @Test
    public void testRedirectPaymentFlow() {
        String url = null;

        try (BufferedReader reader = new BufferedReader(new FileReader("redirect_url.txt"))) {
            String line = reader.readLine();
            if (line != null && line.startsWith("RedirectToUrl=")) {
                url = line.substring("RedirectToUrl=".length()).trim();
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read redirect_url.txt: " + e.getMessage());
            return;
        }

        if (url == null || url.isEmpty()) {
            System.err.println("❌ URL is empty or not found in redirect_url.txt");
            return;
        }

        WebDriverManager.chromedriver().setup();
        WebDriver driver = new ChromeDriver();
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(15));
        driver.manage().window().maximize();

        driver.get(url);
        System.out.println("🌐 Opened URL: " + url);

        try {
            Thread.sleep(3000);

            WebElement cardElement = driver.findElement(
                    By.xpath("//div[contains(@class, 'cursor-pointer') and .//img[@alt='MASTERCARD']]")
            );
            cardElement.click();
            System.out.println("✅ Clicked card containing MASTERCARD image");

            WebElement cvvField = wait.until(ExpectedConditions.elementToBeClickable(By.id("cvc")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", cvvField);
            Thread.sleep(500);
            cvvField.click();
            Thread.sleep(300);
            cvvField.sendKeys("123");
            System.out.println("✅ CVV entered successfully");

            WebElement payButton = wait.until(ExpectedConditions.elementToBeClickable(By.id("pay-button")));
            ((JavascriptExecutor) driver).executeScript("arguments[0].scrollIntoView(true);", payButton);
            Thread.sleep(300);
            payButton.click();
            System.out.println("✅ Pay button clicked");

        } catch (Exception e) {
            System.err.println("❌ Error while interacting with card, CVV, or Pay button: " + e.getMessage());
        }

        try {
            Thread.sleep(25000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        driver.quit();
    }
}
