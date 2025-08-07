package example.tests.ClientSide;

import example.config.Environment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;

public class ListFeesGatewaysTest {

    @Test
    public void testListFeesGateways() {
        String timeslotId = null;
        String token = null;

        // ✅ Read Client Token
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

        // ✅ Read Timeslot ID
        try (BufferedReader reader = new BufferedReader(new FileReader("created_timeslot_id.txt"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("TimeslotId=")) {
                    timeslotId = line.substring("TimeslotId=".length()).trim();
                    break;
                }
            }
        } catch (IOException e) {
            System.err.println("❌ Failed to read Timeslot ID: " + e.getMessage());
            return;
        }

        if (timeslotId == null || timeslotId.isEmpty()) {
            System.err.println("❌ Timeslot ID not found or empty in created_timeslot_id.txt.");
            return;
        }

        // ✅ Construct URL dynamically
        String baseUrl = Environment.getBaseUrl("nest");
        String endpoint = String.format("client/reservation/gateways/list-fees?tSlots=%s", timeslotId);
        String url = baseUrl + "/" + endpoint;

        System.out.println("✅ Requesting list-fees for Timeslot ID: " + timeslotId);
        System.out.println("🔑 Using token: " + token);
        System.out.println("📡 Sending GET request to: " + url);

        // ✅ Send GET request
        Response response = RestAssured
                .given()
                .header("shezlongtoken", token)
                .when()
                .get(url);

        // ✅ Output response
        response.prettyPrint();

        // ✅ Check status code
        if (response.statusCode() == 200) {
            System.out.println("✅ Request successful (Status Code: 200)");
        } else {
            System.err.println("❌ Request failed (Status Code: " + response.statusCode() + ")");
        }
    }
}
