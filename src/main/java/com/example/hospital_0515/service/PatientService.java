package com.example.hospital_0515.service;

import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;



@Service
public class PatientService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void generateRandomPatients (int count) {
        List<Patient> patients = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Patient patient = new Patient();
            patient.setIdCard("ID" + i);
            patient.setName("Patient " + i);
            patient.setGender(i % 2 == 0 ? "Male" : "Female");
            patient.setAge(20 + i % 10);
            patient.setAddress("Address " + i);
            patient.setContact("Contact " + i);
            patient.setCreditScore(100);
            patient.setMedicalRecord("Medical Record Empty");
            patient.setUsername("patient" + i);
            patient.setPassword(passwordEncoder.encode("p" + i));
            patient.setStatus("APPROVED");
            patients.add(patient);
        }
        patientRepository.saveAll(patients);
    }
}