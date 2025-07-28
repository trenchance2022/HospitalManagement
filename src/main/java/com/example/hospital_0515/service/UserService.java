//UserService.java
package com.example.hospital_0515.service;

import com.example.hospital_0515.model.Admin;
import com.example.hospital_0515.model.Doctor;
import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.repository.AdminRepository;
import com.example.hospital_0515.repository.DoctorRepository;
import com.example.hospital_0515.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class UserService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AdminRepository adminRepository;

    // 其他业务逻辑方法
}