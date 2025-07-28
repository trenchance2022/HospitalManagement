package com.example.hospital_0515.controller;

import com.example.hospital_0515.model.Bid;
import com.example.hospital_0515.model.Visit;
import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.repository.BidRepository;
import com.example.hospital_0515.repository.VisitRepository;
import com.example.hospital_0515.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.stream.Collectors;
import java.time.LocalDateTime;
import java.util.List;
import java.util.*;
import java.util.Comparator;
import org.springframework.http.HttpStatus;

@RestController // 标注这是一个RESTful控制器
@RequestMapping("/api/bids") // 为该控制器中的所有请求映射统一的URL前缀
public class BidController {

    @Autowired // 自动注入BidRepository
    private BidRepository bidRepository;

    @Autowired // 自动注入VisitRepository
    private VisitRepository visitRepository;

    @Autowired // 自动注入PatientRepository
    private PatientRepository patientRepository;

    /**
     * 处理竞价请求
     * @param visitId 挂号ID
     * @param amount 竞拍金额
     * @param principal 当前认证用户信息
     * @return 返回响应实体
     */
    @PostMapping("/place")
    public ResponseEntity<?> placeBid(@RequestParam Long visitId, @RequestParam double amount, Principal principal) {
        String patientUsername = principal.getName(); // 获取当前用户的用户名
        LocalDateTime now = LocalDateTime.now(); // 获取当前时间

        // 获取患者信息
        Patient patient = patientRepository.findByUsername(patientUsername);
        if (patient == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body("患者不存在");
        }

        // 检查患者信用分
        if (patient.getCreditScore() < 60) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).body("您的信用分低于60，无法参与竞拍");
        }

        // 创建新的竞拍对象
        Bid bid = new Bid();
        bid.setVisitId(visitId);
        bid.setPatientUsername(patientUsername);
        bid.setBidAmount(amount);
        bid.setBidTime(now);

        // 保存竞拍信息到数据库
        bidRepository.save(bid);
        return ResponseEntity.ok().build();
    }

    /**
     * 获取当前患者的所有竞价记录
     * @param principal 当前认证用户信息
     * @return 返回竞价记录的响应实体
     */
    @GetMapping("/patient-bids")
    public ResponseEntity<List<Bid>> getPatientBids(Principal principal) {
        String patientUsername = principal.getName(); // 获取当前用户的用户名
        return ResponseEntity.ok(bidRepository.findByPatientUsername(patientUsername)); // 查询并返回该用户的所有竞价记录
    }

    /**
     * 获取特定挂号ID的所有竞价记录
     * @param visitId 挂号ID
     * @return 返回竞价记录的响应实体
     */
    @GetMapping("/visit-bids/{visitId}")
    public ResponseEntity<List<Bid>> getVisitBids(@PathVariable Long visitId) {
        return ResponseEntity.ok(bidRepository.findByVisitId(visitId)); // 查询并返回该挂号ID的所有竞价记录
    }

    /**
     * 获取特定挂号ID的前五名竞价记录
     * @param visitId 挂号ID
     * @return 返回前五名竞价记录的响应实体
     */
    @GetMapping("/top-bids/{visitId}")
    public ResponseEntity<List<Map<String, Object>>> getTopBids(@PathVariable Long visitId) {
        List<Bid> bids = bidRepository.findByVisitId(visitId); // 获取指定挂号ID的所有竞价记录

        // 获取每个竞价记录的患者信息并构建结果集
        List<Map<String, Object>> result = bids.stream().map(bid -> {
            Patient patient = patientRepository.findByUsername(bid.getPatientUsername());
            Map<String, Object> bidInfo = new HashMap<>();
            bidInfo.put("patientUsername", bid.getPatientUsername());
            bidInfo.put("bidAmount", bid.getBidAmount());
            bidInfo.put("bidTime", bid.getBidTime());
            bidInfo.put("creditScore", patient.getCreditScore());
            return bidInfo;
        }).collect(Collectors.toList());

        // 根据竞拍金额和信用分进行排序并限制前5名
        result = result.stream()
                .sorted(Comparator.comparingDouble(b -> {
                    double bidAmount = (double) ((Map<String, Object>) b).get("bidAmount");
                    int creditScore = (int) ((Map<String, Object>) b).get("creditScore");
                    return bidAmount * creditScore;
                }).reversed())
                .limit(5)
                .collect(Collectors.toList());

        return ResponseEntity.ok(result); // 返回前五名竞价记录
    }
}
