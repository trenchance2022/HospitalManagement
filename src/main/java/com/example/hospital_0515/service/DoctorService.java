package com.example.hospital_0515.service;

import com.example.hospital_0515.model.Doctor;
import com.example.hospital_0515.repository.DoctorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.ArrayList;
import java.util.List;



@Service
public class DoctorService {

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public void generateRandomDoctors(int count) {
        List<Doctor> doctors = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            Doctor doctor = new Doctor();
            doctor.setIdCard("ID" + i);
            doctor.setName("Doctor " + i);
            doctor.setDepartment("Department " + i);
            doctor.setTitle("Title " + i);
            doctor.setUsername("doctor" + i);
            doctor.setPassword(passwordEncoder.encode("p" + i));
            doctor.setAge(30 + i % 10);
            doctor.setGender(i % 2 == 0 ? "Male" : "Female");
            doctor.setAddress("Address " + i);
            doctor.setContact("Contact " + i);
            doctor.setHospital("Hospital " + i);
            doctor.setSpecialty("Specialty " + i);
            doctor.setStatus("APPROVED");
            doctors.add(doctor);
        }
        doctorRepository.saveAll(doctors);
    }
}
