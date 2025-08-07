package example.tests.OperatorSide;

import example.config.Environment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class PaymentAttemptRequest {

    @Test
    public void validatePaymentAttempts() {
        String token = null;
        String orderId = null;

        // ✅ Read Operator Token from file
        try (BufferedReader reader = new BufferedReader(new FileReader("TokenOperator.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("AccessToken=")) {
                    token = line.substring("AccessToken=".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read TokenOperator.txt: " + e.getMessage());
            return;
        }

        if (token == null || token.isEmpty()) {
            System.err.println("❌ Operator token not found.");
            return;
        }

        // ✅ Read Order ID from redirect_url.txt
        try (BufferedReader reader = new BufferedReader(new FileReader("redirect_url.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("OrderId=")) {
                    orderId = line.substring("OrderId=".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read OrderId: " + e.getMessage());
            return;
        }

        if (orderId == null || orderId.isEmpty()) {
            System.err.println("❌ OrderId not found.");
            return;
        }

        // ✅ Format dates (today and tomorrow)
        LocalDate today = LocalDate.now();
        LocalDate tomorrow = today.plusDays(1);
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
        String dateFrom = today.format(formatter);
        String dateTo = tomorrow.format(formatter);

        // ✅ Compose URL dynamically
        String baseUrl = Environment.getBaseUrl("node");
        String endpoint = String.format(
                "shezlong-operator/payments/paymentAttempts?page=1&dateFrom=%s&dateTo=%s&gatewayTransactionId=%s",
                dateFrom, dateTo, orderId
        );
        String fullUrl = baseUrl + "/" + endpoint;

        System.out.println("🌐 Sending GET request to: " + fullUrl);

        try {
            Thread.sleep(2000);
        } catch (InterruptedException e) {
            throw new RuntimeException(e);
        }

        // ✅ Send GET request
        Response response = RestAssured
                .given()
                .header("shezlongtoken", token)
                .get(fullUrl);

        // ✅ Print and validate response
        response.prettyPrint();

        if (response.statusCode() == 200) {
            System.out.println("✅ Payment attempt validated with status 200");

            // ✅ Extract and print transaction status
            String transactionStatus = response.jsonPath().getString("data.data[0].transactionStatus");
            System.out.println("📄 Transaction Status: " + transactionStatus);

            if ("success".equalsIgnoreCase(transactionStatus)) {
                System.out.println("✅ Transaction completed successfully!");
            } else {
                System.err.println("❌ Transaction not successful: " + transactionStatus);
            }
        } else {
            System.err.println("❌ Validation failed with status: " + response.statusCode());
        }
    }
}
