package com.example.hospital_0515.util;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AdminOperationLogger {

    private static final String LOG_DIRECTORY = "./UserLogger";

    public static void logOperation(String operationType, String operatedBy, String operatedOn, Long targetId) {
        LocalDateTime timestamp = LocalDateTime.now();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        String logMessage = String.format("%s - Operation: %s, Operated by: %s, Operated on: %s, Target ID: %d",
                timestamp.format(formatter), operationType, operatedBy, operatedOn, targetId);

        try (FileWriter fileWriter = new FileWriter(LOG_DIRECTORY + "/admin.log", true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            printWriter.println(logMessage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
