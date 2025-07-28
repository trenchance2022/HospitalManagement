//UserController.java
package com.example.hospital_0515.controller;

import com.example.hospital_0515.model.Admin;
import com.example.hospital_0515.model.Doctor;
import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.model.Visit;
import com.example.hospital_0515.repository.AdminRepository;
import com.example.hospital_0515.repository.DoctorRepository;
import com.example.hospital_0515.repository.PatientRepository;
import com.example.hospital_0515.repository.VisitRepository;
import com.example.hospital_0515.util.AdminOperationLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.*;
import org.springframework.http.HttpStatus;

@RestController // 标记这是一个RESTful控制器
@RequestMapping("/api/users") // 所有的请求都将在此基础上
public class UserController {

    @Autowired // 自动注入 PatientRepository
    private PatientRepository patientRepository;

    @Autowired // 自动注入 DoctorRepository
    private DoctorRepository doctorRepository;

    @Autowired // 自动注入 AdminRepository
    private AdminRepository adminRepository;

    @Autowired // 自动注入 PasswordEncoder
    private PasswordEncoder passwordEncoder;

    @Autowired // 自动注入 VisitRepository
    private VisitRepository visitRepository;

    // 初始化方法，创建默认的管理员账号
    @PostConstruct
    public void init() {
        if (adminRepository.findByUsername("admin") == null) {
            Admin admin = new Admin();
            admin.setIdCard("admin");
            admin.setName("Admin");
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin"));
            admin.setStatus("APPROVED");
            adminRepository.save(admin);
        }
    }

    // 检查用户名是否已存在
    @GetMapping("/check-username")
    public ResponseEntity<?> checkUsername(@RequestParam String username) {
        boolean exists = patientRepository.existsByUsername(username) ||
                doctorRepository.existsByUsername(username) ||
                adminRepository.existsByUsername(username);
        if (exists) {
            return ResponseEntity.status(HttpStatus.CONFLICT).body("用户名已存在");
        } else {
            return ResponseEntity.ok("用户名可用");
        }
    }

    // 获取当前医生预订的患者列表
    @GetMapping("/patients/booked-by-current-doctor")
    public ResponseEntity<List<Patient>> getPatientsBookedByCurrentDoctor() {
        String currentDoctorUsername = getCurrentDoctorUsername(); // 获取当前医生的用户名
        List<Visit> visits = visitRepository.findByDoctorName(currentDoctorUsername); // 获取该医生的所有挂号信息
        Set<String> patientUsernames = new HashSet<>();
        for (Visit visit : visits) {
            patientUsernames.addAll(visit.getBookedBy()); // 获取所有预约该医生的患者用户名
        }
        List<Patient> patients = patientRepository.findByUsernameIn(patientUsernames); // 查询所有患者信息
        return ResponseEntity.ok(patients);
    }

    // 注册患者
    @PostMapping("/register/patient")
    public ResponseEntity<Patient> registerPatient(@RequestBody Patient patient) {
        patient.setPassword(passwordEncoder.encode(patient.getPassword())); // 加密密码
        patient.setStatus("PENDING"); // 设置状态为待审批
        return ResponseEntity.ok(patientRepository.save(patient)); // 保存患者信息并返回
    }

    // 注册医生
    @PostMapping("/register/doctor")
    public ResponseEntity<Doctor> registerDoctor(@RequestBody Doctor doctor) {
        doctor.setPassword(passwordEncoder.encode(doctor.getPassword())); // 加密密码
        doctor.setStatus("PENDING"); // 设置状态为待审批
        return ResponseEntity.ok(doctorRepository.save(doctor)); // 保存医生信息并返回
    }

    // 注册管理员
    @PostMapping("/register/admin")
    public ResponseEntity<Admin> registerAdmin(@RequestBody Admin admin) {
        admin.setPassword(passwordEncoder.encode(admin.getPassword())); // 加密密码
        admin.setStatus("PENDING"); // 设置状态为待审批
        return ResponseEntity.ok(adminRepository.save(admin)); // 保存管理员信息并返回
    }

    // 更新用户信息
    @PutMapping("/update/{role}/{id}")
    public ResponseEntity<?> updateUser(@PathVariable String role, @PathVariable Long id, @RequestBody Map<String, Object> updates) {
        String currentAdminUsername = getCurrentAdminUsername(); // 获取当前管理员用户名
        switch (role) {
            case "patient":
                Patient patient = patientRepository.findById(id).orElseThrow(); // 查找患者
                if (updates.containsKey("name")) patient.setName((String) updates.get("name"));
                if (updates.containsKey("medicalRecord")) patient.setMedicalRecord((String) updates.get("medicalRecord"));
                if (updates.containsKey("age")) patient.setAge((Integer) updates.get("age"));
                if (updates.containsKey("gender")) patient.setGender((String) updates.get("gender"));
                if (updates.containsKey("address")) patient.setAddress((String) updates.get("address"));
                if (updates.containsKey("contact")) patient.setContact((String) updates.get("contact"));
                patientRepository.save(patient); // 保存更新信息
                AdminOperationLogger.logOperation("UPDATE", currentAdminUsername, role, id); // 记录操作日志
                break;
            case "doctor":
                Doctor doctor = doctorRepository.findById(id).orElseThrow(); // 查找医生
                if (updates.containsKey("name")) doctor.setName((String) updates.get("name"));
                if (updates.containsKey("department")) doctor.setDepartment((String) updates.get("department"));
                if (updates.containsKey("title")) doctor.setTitle((String) updates.get("title"));
                if (updates.containsKey("age")) doctor.setAge((Integer) updates.get("age"));
                if (updates.containsKey("gender")) doctor.setGender((String) updates.get("gender"));
                if (updates.containsKey("address")) doctor.setAddress((String) updates.get("address"));
                if (updates.containsKey("contact")) doctor.setContact((String) updates.get("contact"));
                if (updates.containsKey("hospital")) doctor.setHospital((String) updates.get("hospital"));
                if (updates.containsKey("specialty")) doctor.setSpecialty((String) updates.get("specialty"));
                doctorRepository.save(doctor); // 保存更新信息
                AdminOperationLogger.logOperation("UPDATE", currentAdminUsername, role, id); // 记录操作日志
                break;
            case "admin":
                Admin admin = adminRepository.findById(id).orElseThrow(); // 查找管理员
                if (updates.containsKey("name")) admin.setName((String) updates.get("name"));
                if (updates.containsKey("address")) admin.setAddress((String) updates.get("address"));
                if (updates.containsKey("contact")) admin.setContact((String) updates.get("contact"));
                adminRepository.save(admin); // 保存更新信息
                AdminOperationLogger.logOperation("UPDATE", currentAdminUsername, role, id); // 记录操作日志
                break;
        }
        return ResponseEntity.ok().build();
    }

    // 删除用户
    @DeleteMapping("/delete/{role}/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable String role, @PathVariable Long id) {
        String currentAdminUsername = getCurrentAdminUsername(); // 获取当前管理员用户名
        switch (role) {
            case "patient":
                patientRepository.deleteById(id); // 删除患者
                AdminOperationLogger.logOperation("DELETE", currentAdminUsername, role, id); // 记录操作日志
                break;
            case "doctor":
                doctorRepository.deleteById(id); // 删除医生
                AdminOperationLogger.logOperation("DELETE", currentAdminUsername, role, id); // 记录操作日志
                break;
            case "admin":
                adminRepository.deleteById(id); // 删除管理员
                AdminOperationLogger.logOperation("DELETE", currentAdminUsername, role, id); // 记录操作日志
                break;
        }
        return ResponseEntity.ok().build();
    }

    // 获取所有待审批的患者
    @GetMapping("/pending/patients")
    public ResponseEntity<List<Patient>> getPendingPatients() {
        return ResponseEntity.ok(patientRepository.findByStatus("PENDING"));
    }

    // 获取所有待审批的医生
    @GetMapping("/pending/doctors")
    public ResponseEntity<List<Doctor>> getPendingDoctors() {
        return ResponseEntity.ok(doctorRepository.findByStatus("PENDING"));
    }

    // 获取所有待审批的管理员
    @GetMapping("/pending/admins")
    public ResponseEntity<List<Admin>> getPendingAdmins() {
        return ResponseEntity.ok(adminRepository.findByStatus("PENDING"));
    }

    // 审批用户
    @PutMapping("/approve/{role}/{id}")
    public ResponseEntity<?> approveUser(@PathVariable String role, @PathVariable Long id) {
        switch (role) {
            case "patient":
                Patient patient = patientRepository.findById(id).orElseThrow(); // 查找患者
                patient.setStatus("APPROVED"); // 设置状态为已审批
                patientRepository.save(patient); // 保存更新信息
                break;
            case "doctor":
                Doctor doctor = doctorRepository.findById(id).orElseThrow(); // 查找医生
                doctor.setStatus("APPROVED"); // 设置状态为已审批
                doctorRepository.save(doctor); // 保存更新信息
                break;
            case "admin":
                Admin admin = adminRepository.findById(id).orElseThrow(); // 查找管理员
                admin.setStatus("APPROVED"); // 设置状态为已审批
                adminRepository.save(admin); // 保存更新信息
                break;
        }
        return ResponseEntity.ok().build();
    }

    // 获取所有已审批的患者
    @GetMapping("/patients")
    public ResponseEntity<List<Patient>> getAllPatients() {
        return ResponseEntity.ok(patientRepository.findByStatus("APPROVED"));
    }

    // 获取所有已审批的医生
    @GetMapping("/doctors")
    public ResponseEntity<List<Doctor>> getAllDoctors() {
        return ResponseEntity.ok(doctorRepository.findByStatus("APPROVED"));
    }

    // 获取所有已审批的管理员
    @GetMapping("/admins")
    public ResponseEntity<List<Admin>> getAllAdmins() {
        return ResponseEntity.ok(adminRepository.findByStatus("APPROVED"));
    }

    // 更改管理员密码
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody Map<String, Object> updates) {
        Admin admin = adminRepository.findByUsername("admin");
        if (admin != null && admin.isFirstLogin()) { // 检查是否是第一次登录
            String newPassword = (String) updates.get("password");
            admin.setPassword(passwordEncoder.encode(newPassword)); // 加密新密码
            admin.setFirstLogin(false); // 设置为已登录
            adminRepository.save(admin); // 保存更新信息
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.badRequest().build();
    }

    // 更新患者信用分
    @PutMapping("/update-credit-score")
    public ResponseEntity<?> updateCreditScore(@RequestBody Map<String, Object> updates) {
        String username = (String) updates.get("username");
        int score = (Integer) updates.get("score");
        String currentDoctorUsername = getCurrentDoctorUsername(); // 获取当前医生用户名

        Patient patient = patientRepository.findByUsername(username);
        if (patient != null) {
            List<Visit> visits = visitRepository.findByDoctorNameAndBookedByContaining(currentDoctorUsername, username);
            if (!visits.isEmpty()) { // 检查医生是否有权限修改患者信用分
                patient.setCreditScore(score); // 更新信用分
                patientRepository.save(patient); // 保存更新信息
                return ResponseEntity.ok().build();
            } else {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您没有权限修改此患者的信用分");
            }
        } else {
            return ResponseEntity.badRequest().body("患者不存在");
        }
    }

    // 获取当前医生用户名
    private String getCurrentDoctorUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // 获取当前管理员用户名
    private String getCurrentAdminUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // 获取当前用户名
    private String getCurrentUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    // 更新用户自己的信息和密码
    @PutMapping("/update-self/{role}")
    public ResponseEntity<?> updateSelf(@PathVariable String role, @RequestBody Map<String, Object> updates) {
        String currentUsername = getCurrentUsername(); // 获取当前用户名
        switch (role) {
            case "patient":
                Patient patient = patientRepository.findByUsername(currentUsername);
                if (patient == null) {
                    return ResponseEntity.badRequest().build();
                }
                if (updates.containsKey("name")) patient.setName((String) updates.get("name"));
                if (updates.containsKey("medicalRecord")) patient.setMedicalRecord((String) updates.get("medicalRecord"));
                if (updates.containsKey("age")) patient.setAge((Integer) updates.get("age"));
                if (updates.containsKey("gender")) patient.setGender((String) updates.get("gender"));
                if (updates.containsKey("address")) patient.setAddress((String) updates.get("address"));
                if (updates.containsKey("contact")) patient.setContact((String) updates.get("contact"));
                if (updates.containsKey("password")) patient.setPassword(passwordEncoder.encode((String) updates.get("password")));
                patientRepository.save(patient); // 保存更新信息
                break;
            case "doctor":
                Doctor doctor = doctorRepository.findByUsername(currentUsername);
                if (doctor == null) {
                    return ResponseEntity.badRequest().build();
                }
                if (updates.containsKey("name")) doctor.setName((String) updates.get("name"));
                if (updates.containsKey("department")) doctor.setDepartment((String) updates.get("department"));
                if (updates.containsKey("title")) doctor.setTitle((String) updates.get("title"));
                if (updates.containsKey("age")) doctor.setAge((Integer) updates.get("age"));
                if (updates.containsKey("gender")) doctor.setGender((String) updates.get("gender"));
                if (updates.containsKey("address")) doctor.setAddress((String) updates.get("address"));
                if (updates.containsKey("contact")) doctor.setContact((String) updates.get("contact"));
                if (updates.containsKey("hospital")) doctor.setHospital((String) updates.get("hospital"));
                if (updates.containsKey("specialty")) doctor.setSpecialty((String) updates.get("specialty"));
                if (updates.containsKey("password")) doctor.setPassword(passwordEncoder.encode((String) updates.get("password")));
                doctorRepository.save(doctor); // 保存更新信息
                break;
            case "admin":
                Admin admin = adminRepository.findByUsername(currentUsername);
                if (admin == null) {
                    return ResponseEntity.badRequest().build();
                }
                if (updates.containsKey("name")) admin.setName((String) updates.get("name"));
                if (updates.containsKey("address")) admin.setAddress((String) updates.get("address"));
                if (updates.containsKey("contact")) admin.setContact((String) updates.get("contact"));
                if (updates.containsKey("password")) admin.setPassword(passwordEncoder.encode((String) updates.get("password")));
                adminRepository.save(admin); // 保存更新信息
                break;
        }
        return ResponseEntity.ok().build();
    }

    @GetMapping("/current")
    public ResponseEntity<?> getCurrentUser() {
        String username = getCurrentUsername();
        Admin admin = adminRepository.findByUsername(username);
        if (admin != null) {
            return ResponseEntity.ok(admin);
        }
        Doctor doctor = doctorRepository.findByUsername(username);
        if (doctor != null) {
            return ResponseEntity.ok(doctor);
        }
        Patient patient = patientRepository.findByUsername(username);
        if (patient != null) {
            return ResponseEntity.ok(patient);
        }
        return ResponseEntity.badRequest().body("用户不存在");
    }


}

// Path: src/main/java/com/example/hospital_0515/repository/AdminRepository.java