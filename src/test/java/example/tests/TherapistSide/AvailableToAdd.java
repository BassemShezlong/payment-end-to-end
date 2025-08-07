package example.tests.TherapistSide;

import example.config.Environment;
import io.restassured.RestAssured;
import io.restassured.path.json.JsonPath;
import io.restassured.response.Response;
import org.testng.annotations.Test;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

public class AvailableToAdd {

    @Test
    public void getAvailableTimeSlots() {
        // Step 1: Read token from Variables file
        String token = null;
        try (BufferedReader reader = new BufferedReader(new FileReader("Variables"))) {
            String line;
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("AccessToken=")) {
                    token = line.substring("AccessToken=".length()).trim();
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (token == null) {
            System.err.println("❌ AccessToken not found in Variables file.");
            return;
        }

        // Step 2: Calculate dynamicDayDate = today + 1
        String dynamicDayDate = LocalDate.now().plusDays(1)
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd"));

        // Step 3: Construct URL dynamically
        String baseUrl = Environment.getBaseUrl("node");
        String endpoint = String.format(
                "therapist-api/tSlots?monthsAhead=0&servId=1&dayDate=%s&futureOnly=true&getAvailableToAdd=true",
                dynamicDayDate
        );
        String url = baseUrl + "/" + endpoint;

        // Step 4: Send GET request with token in header
        Response response = RestAssured
                .given()
                .header("shezlongtoken", token)
                .when()
                .get(url);

        // Step 5: Print response
        response.prettyPrint();

        // Step 6: Validate response code
        response.then().statusCode(200);
        System.out.println("✅ Time slots fetched successfully for date: " + dynamicDayDate);

        // Step 7: Extract tSlotDate, tSlotTime, servId from availableToAdd
        JsonPath jsonPath = response.jsonPath();
        List<Map<String, Object>> tSlots = jsonPath.getList("data.tSlots");

        boolean found = false;

        for (Map<String, Object> slot : tSlots) {
            String tSlotDate = (String) slot.get("tSlotDate");
            List<Map<String, Object>> availableToAddList = (List<Map<String, Object>>) slot.get("availableToAdd");

            if (availableToAddList != null) {
                for (Map<String, Object> available : availableToAddList) {
                    String tSlotTime = (String) available.get("tSlotTime");
                    int servId = (int) available.get("servId");

                    System.out.println("✅ Found available slot:");
                    System.out.println("  📅 Date: " + tSlotDate);
                    System.out.println("  ⏰ Time: " + tSlotTime);
                    System.out.println("  🛠️ Service ID: " + servId);

                    // Step 8: Save slot data to file
                    try (FileWriter writer = new FileWriter("available_slot_info.txt")) {
                        writer.write("tSlotDate=" + tSlotDate + "\n");
                        writer.write("tSlotTime=" + tSlotTime + "\n");
                        writer.write("servId=" + servId + "\n");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    found = true;
                    break; // stop after first match
                }
            }

            if (found) break;
        }

        if (!found) {
            System.out.println("⚠️ No availableToAdd slots found.");
        }
    }
}
