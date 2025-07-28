package com.example.hospital_0515.controller;

import com.example.hospital_0515.model.Visit;
import com.example.hospital_0515.repository.VisitRepository;
import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.model.Doctor;
import com.example.hospital_0515.repository.PatientRepository;
import com.example.hospital_0515.repository.DoctorRepository;
import com.example.hospital_0515.util.PatientBookingLogger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.time.DayOfWeek;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.stream.Collectors;
import java.time.temporal.ChronoUnit;
import java.util.*;

@RestController
@RequestMapping("/api/visits")
public class VisitController {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    /**
     * 根据ID获取出诊信息
     */
    @GetMapping("/{id}")
    public ResponseEntity<Map<String, Object>> getVisitById(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        Map<String, Object> visitDetails = new HashMap<>();
        visitDetails.put("visit", visit);
        Doctor doctor = doctorRepository.findByUsername(visit.getDoctorName());
        if (doctor != null) {
            visitDetails.put("hospital", doctor.getHospital());
        }
        return ResponseEntity.ok(visitDetails);
    }

    /**
     * 创建新的普通出诊信息
     */
    @PostMapping("/create")
    public ResponseEntity<Visit> createVisit(@RequestBody Visit visit) {
        String currentDoctorUsername = getCurrentDoctorUsername();
        visit.setDoctorName(currentDoctorUsername);
        visit.setStatus("PENDING");
        visit.setAuction(false);  // 设置为非竞拍号源
        return ResponseEntity.ok(visitRepository.save(visit));
    }

    /**
     * 创建新的竞拍出诊信息
     */
    @PostMapping("/create-auction")
    public ResponseEntity<Visit> createAuctionVisit(@RequestBody Visit visit) {
        String currentDoctorUsername = getCurrentDoctorUsername();
        visit.setDoctorName(currentDoctorUsername);
        visit.setStatus("PENDING");
        visit.setAuction(true);  // 设置为竞拍号源
        return ResponseEntity.ok(visitRepository.save(visit));
    }

    /**
     * 创建新的周期性出诊信息
     */
    @PostMapping("/recurring/create")
    public ResponseEntity<Visit> createRecurringVisit(@RequestBody Visit visit) {
        visit.setRecurring(true);
        visit.setStatus("PENDING");
        String currentDoctorUsername = getCurrentDoctorUsername();
        visit.setDoctorName(currentDoctorUsername);
        visit.setAuction(false);  // 设置为非竞拍号源
        return ResponseEntity.ok(visitRepository.save(visit));
    }

    /**
     * 获取所有周期性出诊信息
     */
    @GetMapping("/recurring")
    public ResponseEntity<List<Visit>> getAllRecurringVisits() {
        String currentDoctorUsername = getCurrentDoctorUsername();
        return ResponseEntity.ok(visitRepository.findByDoctorNameAndRecurringAndAuction(currentDoctorUsername, true, false));
    }

    /**
     * 删除周期性出诊信息
     */
    @DeleteMapping("/recurring/delete/{id}")
    public ResponseEntity<?> deleteRecurringVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        String currentDoctorUsername = getCurrentDoctorUsername();
        if (visit.getDoctorName().equals(currentDoctorUsername)) {
            visitRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 获取所有待审批的普通出诊信息
     */
    @GetMapping("/pending")
    public ResponseEntity<List<Visit>> getPendingVisits() {
        return ResponseEntity.ok(visitRepository.findByRecurringAndStatusAndAuction(false, "PENDING", false));
    }

    /**
     * 审批普通出诊信息
     */
    @PutMapping("/approve/{id}")
    public ResponseEntity<?> approveVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        visit.setStatus("APPROVED");
        visitRepository.save(visit);
        return ResponseEntity.ok().build();
    }

    /**
     * 审批竞拍出诊信息
     */
    @PutMapping("/approve-auction/{id}")
    public ResponseEntity<?> approveAuctionVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        visit.setStatus("APPROVED");
        visitRepository.save(visit);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取医生的所有普通出诊信息
     */
    @GetMapping("/normal")
    public ResponseEntity<List<Visit>> getDoctorNormalVisits() {
        String currentDoctorUsername = getCurrentDoctorUsername();
        List<Visit> visits = visitRepository.findByDoctorNameAndNormal(currentDoctorUsername);
        return ResponseEntity.ok(visits);
    }

    /**
     * 获取所有待审批的竞拍出诊信息
     */
    @GetMapping("/pending-auction")
    public ResponseEntity<List<Visit>> getPendingAuctionVisits() {
        return ResponseEntity.ok(visitRepository.findByStatusAndAuction("PENDING", true));
    }

    /**
     * 获取医生的所有竞拍出诊信息
     */
    @GetMapping("/auction")
    public ResponseEntity<List<Visit>> getDoctorAuctionVisits() {
        String currentDoctorUsername = getCurrentDoctorUsername();
        List<Visit> visits = visitRepository.findByDoctorNameAndAuction(currentDoctorUsername);
        return ResponseEntity.ok(visits);
    }

    /**
     * 获取所有可用的竞拍出诊信息
     */
    @GetMapping("/available-auction")
    public ResponseEntity<List<Visit>> getAvailableAuctionVisits() {
        List<Visit> visits = visitRepository.findByStatusAndAuction("APPROVED", true);
        return ResponseEntity.ok(visits);
    }

    /**
     * 删除竞拍出诊信息
     */
    @DeleteMapping("/auction/delete/{id}")
    public ResponseEntity<?> deleteAuctionVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        String currentDoctorUsername = getCurrentDoctorUsername();
        if (visit.getDoctorName().equals(currentDoctorUsername)) {
            visitRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 获取指定时间段内医生的所有出诊信息
     */
    @GetMapping("/doctor")
    public ResponseEntity<List<Visit>> getDoctorVisits(@RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
                                                       @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String currentDoctorUsername = getCurrentDoctorUsername();
        return ResponseEntity.ok(visitRepository.findByDoctorNameAndVisitTimeBetweenAndStatusAndAuction(currentDoctorUsername, startDate, endDate, "APPROVED", false));
    }

    /**
     * 获取可预约的出诊信息，按科室和医生姓名筛选
     */
    @GetMapping("/patient")
    public ResponseEntity<List<Visit>> getAvailableVisits(@RequestParam(required = false) String department,
                                                          @RequestParam(required = false) String doctorName) {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime startOfToday = now.truncatedTo(ChronoUnit.DAYS);
        LocalDateTime endOfToday = startOfToday.plusDays(1);

        // 21日到24日的时间范围
        LocalDateTime oneDayLater = startOfToday.plusDays(1);
        LocalDateTime fourDaysLater = startOfToday.plusDays(4);

        List<Visit> visits = new ArrayList<>();

        // 根据当前时间来决定当天的预约时间范围，上午7点开始开放预约
        if (now.getHour() < 9 && now.getHour() >= 7) {
            // 上午9点前，可以预约当天上午和下午的号
            visits.addAll(visitRepository.findAvailableVisits(department, doctorName, now, endOfToday, false));
        } else if (now.getHour() < 14) {
            // 上午9点后到下午14点前，可以预约当天下午的号
            visits.addAll(visitRepository.findAvailableVisits(department, doctorName, startOfToday.plusHours(12), endOfToday, false));
        }

        // 21日到24日的号
        visits.addAll(visitRepository.findAvailableVisits(department, doctorName, oneDayLater, fourDaysLater, false));

        return ResponseEntity.ok(visits);
    }

    /**
     * 预约出诊信息
     */
    @PutMapping("/book/{id}")
    public ResponseEntity<?> bookVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        String currentPatientUsername = getCurrentPatientUsername();
        Patient patient = patientRepository.findByUsername(currentPatientUsername);

        // 检查患者是否存在
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("患者不存在");
        }

        // 检查是否已挂号
        if (visit.getBookedBy().contains(currentPatientUsername)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("您已挂号，不能重复挂号");
        }

        // 检查信用分
        if (patient.getCreditScore() < 60) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("信用分低于60分，无法预约挂号");
        }

        if (visit.getAvailableSlots() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("该号已挂完");
        }

        return ResponseEntity.ok().build();
    }

    /**
     * 支付预约
     */
    @PutMapping("/pay/{id}")
    public ResponseEntity<?> payVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        String currentPatientUsername = getCurrentPatientUsername();
        Patient patient = patientRepository.findByUsername(currentPatientUsername);

        // 检查患者是否存在
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("患者不存在");
        }

        // 检查是否已挂号
        if (visit.getBookedBy().contains(currentPatientUsername)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("您已挂号，不能重复挂号");
        }

        // 检查信用分
        if (patient.getCreditScore() < 60) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("信用分低于60分，无法预约挂号");
        }

        if (visit.getAvailableSlots() <= 0) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("该号已挂完");
        }

        visit.setAvailableSlots(visit.getAvailableSlots() - 1);
        visit.getBookedBy().add(currentPatientUsername);  // 添加到列表中
        visitRepository.save(visit);  // 保存修改
        PatientBookingLogger.logBooking("BOOK", currentPatientUsername, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 删除出诊信息
     */
    @DeleteMapping("/delete/{id}")
    public ResponseEntity<?> deleteVisit(@PathVariable Long id) {
        String currentDoctorUsername = getCurrentDoctorUsername();
        Visit visit = visitRepository.findById(id).orElseThrow();
        if (visit.getDoctorName().equals(currentDoctorUsername)) {
            visitRepository.deleteById(id);
            return ResponseEntity.ok().build();
        } else {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
    }

    /**
     * 获取所有科室信息
     */
    @GetMapping("/departments")
    public ResponseEntity<List<String>> getDepartments() {
        List<String> departments = visitRepository.findAll().stream()
                .filter(visit -> !visit.isAuction())
                .map(Visit::getDepartment)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(departments);
    }

    /**
     * 获取所有医生信息
     */
    @GetMapping("/doctors")
    public ResponseEntity<List<String>> getDoctors() {
        List<String> doctors = visitRepository.findAll().stream()
                .filter(visit -> !visit.isAuction())
                .map(Visit::getDoctorName)
                .distinct()
                .collect(Collectors.toList());
        return ResponseEntity.ok(doctors);
    }

    /**
     * 获取患者已预约的出诊信息
     */
    @GetMapping("/booked")
    public ResponseEntity<List<Visit>> getBookedVisits() {
        String currentUsername = getCurrentPatientUsername();
        List<Visit> visits = visitRepository.findByBookedByContaining(currentUsername).stream()
                .filter(visit -> !visit.isAuction())
                .collect(Collectors.toList());
        return ResponseEntity.ok(visits);
    }

    /**
     * 获取当前登录医生的用户名
     */
    private String getCurrentDoctorUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    /**
     * 获取当前登录患者的用户名
     */
    private String getCurrentPatientUsername() {
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
        if (principal instanceof UserDetails) {
            return ((UserDetails) principal).getUsername();
        } else {
            return principal.toString();
        }
    }

    /**
     * 获取患者历史挂号信息
     */
    @GetMapping("/history")
    public ResponseEntity<List<Visit>> getPatientHistoryVisits(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime startDate,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime endDate) {
        String currentPatientUsername = getCurrentPatientUsername();
        List<Visit> visits = visitRepository.findByBookedByContainingAndVisitTimeBetween(currentPatientUsername, startDate, endDate).stream()
                .filter(visit -> !visit.isAuction())
                .collect(Collectors.toList());
        return ResponseEntity.ok(visits);
    }

    /**
     * 取消预约
     */
    @PutMapping("/cancel/{id}")
    public ResponseEntity<?> cancelVisit(@PathVariable Long id) {
        Visit visit = visitRepository.findById(id).orElseThrow();
        String currentPatientUsername = getCurrentPatientUsername();

        // 检查是否已挂号
        if (!visit.getBookedBy().contains(currentPatientUsername)) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("您尚未挂号，无法取消");
        }

        // 检查是否在可取消时间范围内
        LocalDateTime now = LocalDateTime.now();
        if (visit.getVisitTime().isBefore(now.plusDays(1))) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("只能在出诊前一天或更早取消预约");
        }

        // 取消预约
        visit.setAvailableSlots(visit.getAvailableSlots() + 1);
        visit.getBookedBy().remove(currentPatientUsername);
        visitRepository.save(visit);
        PatientBookingLogger.logBooking("CANCEL", currentPatientUsername, id);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取推荐的挂号信息
     */
    @GetMapping("/recommendations")
    public ResponseEntity<List<Map<String, Object>>> getRecommendations() {
        String currentPatientUsername = getCurrentPatientUsername();

        // 获取患者之前挂号记录
        List<Visit> previousVisits = visitRepository.findByBookedByContaining(currentPatientUsername);

        // 获取患者挂号的医生的科室
        Set<String> previousDepartments = previousVisits.stream()
                .filter(visit -> !visit.isAuction())
                .map(Visit::getDepartment)
                .collect(Collectors.toSet());

        // 获取同科室的挂号信息
        List<Visit> sameDepartmentVisits = visitRepository.findByDepartmentInAndVisitTimeAfter(previousDepartments, LocalDateTime.now()).stream()
                .filter(visit -> !visit.isAuction())
                .collect(Collectors.toList());

        // 获取所有挂号信息，按出诊时间排序
        List<Visit> allVisits = visitRepository.findByVisitTimeAfterOrderByVisitTimeAsc(LocalDateTime.now()).stream()
                .filter(visit -> !visit.isAuction())
                .collect(Collectors.toList());

        // 获取患者之前挂号的医生
        Set<String> previousDoctors = previousVisits.stream()
                .map(Visit::getDoctorName)
                .collect(Collectors.toSet());

        // 先推荐同科室的医生，再推荐历史挂过号的医生，最后按时间推荐
        List<Visit> recommendations = sameDepartmentVisits.stream()
                .filter(v -> !v.getBookedBy().contains(currentPatientUsername))
                .collect(Collectors.toList());

        recommendations.addAll(allVisits.stream()
                .filter(v -> previousDoctors.contains(v.getDoctorName()) && !v.getBookedBy().contains(currentPatientUsername))
                .collect(Collectors.toList()));

        recommendations.addAll(allVisits.stream()
                .filter(v -> !previousDoctors.contains(v.getDoctorName()) && !v.getBookedBy().contains(currentPatientUsername))
                .collect(Collectors.toList()));

        // 为每个推荐的Visit添加医生的医院信息
        List<Map<String, Object>> recommendationDetails = new ArrayList<>();
        for (Visit visit : recommendations.stream().limit(10).collect(Collectors.toList())) {
            Map<String, Object> recommendationDetail = new HashMap<>();
            recommendationDetail.put("visit", visit);
            Doctor doctor = doctorRepository.findByUsername(visit.getDoctorName());
            if (doctor != null) {
                recommendationDetail.put("hospital", doctor.getHospital());
            }
            recommendationDetails.add(recommendationDetail);
        }

        return ResponseEntity.ok(recommendationDetails);
    }
}


