package com.example.hospital_0515.controller;

import com.example.hospital_0515.service.DoctorService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;


@RestController
@RequestMapping("/api/doctors")
public class DoctorController {

    @Autowired
    private DoctorService doctorService;

    @Autowired // 自动注入 PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @PostMapping("/generate")
    public ResponseEntity<String> generateDoctors(@RequestParam int count) {
        doctorService.generateRandomDoctors(count);
        return ResponseEntity.ok("Successfully generated " + count + " doctors");
    }
}

