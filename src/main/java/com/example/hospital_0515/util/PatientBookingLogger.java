package com.example.hospital_0515.util;

import java.io.*;

public class PatientBookingLogger {

    private static final String LOG_DIR = "./UserLogger";

    public static void logBooking(String operation, String patientUsername, Long visitId) {
        File logDir = new File(LOG_DIR);
        if (!logDir.exists()) {
            logDir.mkdirs();
        }

        String logFileName = LOG_DIR + "/patient.log";
        try (FileWriter fileWriter = new FileWriter(LOG_DIR + "/patient.log", true);
             PrintWriter printWriter = new PrintWriter(fileWriter)) {
            String logEntry = String.format("Operation: %s, Patient: %s, Visit ID: %d, Timestamp: %d\n",
                    operation, patientUsername, visitId, System.currentTimeMillis());
            printWriter.println(logEntry);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
