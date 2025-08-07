package example.tests.ClientSide;

import example.config.Environment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import io.restassured.http.ContentType;
import org.testng.annotations.Test;

import java.io.*;

public class PaymentGatewayRequest {

    @Test
    public void testPaymentGateway() {
        String token = null;
        String timeslotId = null;

        // ✅ Read AccessToken from client_token file
        try (BufferedReader reader = new BufferedReader(new FileReader("client_token"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("AccessToken=")) {
                    token = line.substring("AccessToken=".length()).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (token == null || token.isEmpty()) {
            System.err.println("❌ AccessToken not found in client_token file.");
            return;
        }

        // ✅ Read Timeslot ID from created_timeslot_id.txt
        try (BufferedReader reader = new BufferedReader(new FileReader("created_timeslot_id.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TimeslotId=")) {
                    timeslotId = line.substring("TimeslotId=".length()).trim();
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read Timeslot ID: " + e.getMessage());
            return;
        }

        if (timeslotId == null || timeslotId.isEmpty()) {
            System.err.println("❌ Timeslot ID is missing in created_timeslot_id.txt.");
            return;
        }

        // ✅ Build dynamic URL
        String baseUrl = Environment.getBaseUrl("nest");
        String endpoint = "client/reservation/pay";
        String url = baseUrl + "/" + endpoint;

        // ✅ Prepare request body
        String requestBody = "{\"countryGatewayId\":48,\"slots\":[" + timeslotId + "]}";

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

        // ✅ Output full response
        response.prettyPrint();

        // ✅ Handle result
        if (response.statusCode() == 201) {
            System.out.println("✅ Request succeeded");

            // 🔁 Extract values
            String redirectUrl = response.jsonPath().getString("data.redirectToUrl");
            int orderId = response.jsonPath().getInt("data.orderId");

            System.out.println("🔗 redirectToUrl: " + redirectUrl);
            System.out.println("🧾 orderId: " + orderId);

            // 💾 Save both to file
            try (BufferedWriter writer = new BufferedWriter(new FileWriter("redirect_url.txt"))) {
                writer.write("RedirectToUrl=" + redirectUrl);
                writer.newLine();
                writer.write("OrderId=" + orderId);
                System.out.println("💾 Saved redirectToUrl and orderId to redirect_url.txt");
            } catch (IOException e) {
                System.err.println("❌ Failed to save data: " + e.getMessage());
            }

        } else {
            System.err.println("❌ Request failed (Status Code: " + response.statusCode() + ")");
        }
    }
}
