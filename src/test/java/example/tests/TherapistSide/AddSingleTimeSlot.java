package example.tests.TherapistSide;

import example.config.Environment;
import io.restassured.RestAssured;
import io.restassured.response.Response;
import org.testng.annotations.Test;

import java.io.*;

public class AddSingleTimeSlot {

    @Test
    public void addSingleSlot() {
        String token = null;
        String tSlotDate = null;
        String tSlotTime = null;
        String servId = null;

        // Step 1: Read AccessToken and slot info from files
        try (BufferedReader tokenReader = new BufferedReader(new FileReader("Variables"));
             BufferedReader slotReader = new BufferedReader(new FileReader("available_slot_info.txt"))) {

            String line;
            while ((line = tokenReader.readLine()) != null) {
                if (line.startsWith("AccessToken=")) {
                    token = line.substring("AccessToken=".length()).trim();
                }
            }

            while ((line = slotReader.readLine()) != null) {
                if (line.startsWith("tSlotDate=")) {
                    tSlotDate = line.substring("tSlotDate=".length()).trim();
                } else if (line.startsWith("tSlotTime=")) {
                    tSlotTime = line.substring("tSlotTime=".length()).trim();
                } else if (line.startsWith("servId=")) {
                    servId = line.substring("servId=".length()).trim();
                }
            }

        } catch (IOException e) {
            e.printStackTrace();
        }

        if (token == null || tSlotDate == null || tSlotTime == null || servId == null) {
            System.err.println("❌ Missing token or time slot info.");
            return;
        }

        // Step 2: Build tSlotDateTime as "YYYY-MM-DD HH:mm:ss"
        String tSlotTime24h;
        try {
            java.time.format.DateTimeFormatter inputFormat = java.time.format.DateTimeFormatter.ofPattern("h:mm a");
            java.time.format.DateTimeFormatter outputFormat = java.time.format.DateTimeFormatter.ofPattern("HH:mm:ss");
            tSlotTime24h = java.time.LocalTime.parse(tSlotTime, inputFormat).format(outputFormat);
        } catch (Exception e) {
            System.err.println("❌ Invalid tSlotTime format: " + tSlotTime);
            return;
        }

        String tSlotDateTime = tSlotDate + " " + tSlotTime24h;

        // Step 3: Prepare request body
        String requestBody = String.format("{\"servId\":%s,\"tSlotDateTime\":\"%s\"}", servId, tSlotDateTime);

        // Step 4: Send POST request
        String url = String.format("%s/therapist-api/tSlots/addSingle", Environment.getBaseUrl("node"));
        Response response = RestAssured
                .given()
                .header("shezlongtoken", token)
                .header("Content-Type", "application/json")
                .body(requestBody)
                .when()
                .post(url);

        // Step 5: Print and verify response
        response.prettyPrint();
        response.then().statusCode(200);

        // Step 6: Extract new timeslot ID from response
        int createdId = response.jsonPath().getInt("data.newTimeslot.id");

        // Step 7: Save to file
        try (BufferedWriter writer = new BufferedWriter(new FileWriter("created_timeslot_id.txt"))) {
            writer.write("TimeslotId=" + createdId);
        } catch (IOException e) {
            e.printStackTrace();
        }

        System.out.println("✅ Timeslot added successfully for: " + tSlotDateTime);
        System.out.println("🆔 Created Timeslot ID saved: " + createdId);
    }
}
