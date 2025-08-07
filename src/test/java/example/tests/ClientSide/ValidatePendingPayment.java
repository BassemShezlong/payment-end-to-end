package example.tests.ClientSide;

import example.config.Environment;
import io.restassured.RestAssured;
import io.restassured.http.ContentType;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.*;

public class ValidatePendingPayment {

    @Test
    public void validatePaymentStatus() {
        String token = null;
        String orderId = null;

        // ✅ Read token from client_token
        try (BufferedReader reader = new BufferedReader(new FileReader("client_token"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("AccessToken=")) {
                    token = line.substring("AccessToken=".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading token: " + e.getMessage());
            return;
        }

        if (token == null || token.isEmpty()) {
            System.err.println("❌ Token not found in client_token file.");
            return;
        }

        // ✅ Read orderId from redirect_url.txt
        try (BufferedReader reader = new BufferedReader(new FileReader("redirect_url.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("OrderId=")) {
                    orderId = line.substring("OrderId=".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Error reading orderId: " + e.getMessage());
            return;
        }

        if (orderId == null || orderId.isEmpty()) {
            System.err.println("❌ orderId not found in redirect_url.txt.");
            return;
        }

        // ✅ Build dynamic URL
        String baseUrl = Environment.getBaseUrl("nest");
        String endpoint = "client/reservation/pending-payments/validate";
        String url = baseUrl + "/" + endpoint;

        // ✅ Prepare request body
        String requestBody = "{\"orderId\":" + orderId + "}";

        System.out.println("📡 Sending POST to: " + url);
        System.out.println("🔑 Token: " + token);
        System.out.println("📦 Body: " + requestBody);

        // ✅ Send POST request
        Response response = RestAssured
                .given()
                .header("shezlongtoken", token)
                .contentType(ContentType.JSON)
                .body(requestBody)
                .when()
                .post(url);

        // ✅ Print response
        response.prettyPrint();

        // ✅ Validate response
        if (response.statusCode() == 200) {
            boolean chargeSuccess = response.jsonPath().getBoolean("data.chargeSuccess");
            boolean captured = response.jsonPath().getBoolean("data.captured");
            boolean pending = response.jsonPath().getBoolean("data.pending");

            if (chargeSuccess && captured && !pending) {
                System.out.println("✅ Payment validated successfully: chargeSuccess=true, captured=true, pending=false");
            } else {
                System.err.println("❌ Payment status incorrect:");
                System.err.println("chargeSuccess=" + chargeSuccess + ", captured=" + captured + ", pending=" + pending);
            }
        } else {
            System.err.println("❌ Request failed with status code: " + response.statusCode());
        }
    }
}
