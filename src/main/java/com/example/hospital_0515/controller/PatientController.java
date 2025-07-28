package com.example.hospital_0515.controller;

import com.example.hospital_0515.service.PatientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/patients")
public class PatientController {

    @Autowired
    private PatientService patientService;

    @Autowired // 自动注入 PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @PostMapping("/generate")
    public ResponseEntity<String> generatePatients(@RequestParam int count) {
        patientService.generateRandomPatients(count);
        return ResponseEntity.ok("Successfully generated " + count + " patients");
    }
}