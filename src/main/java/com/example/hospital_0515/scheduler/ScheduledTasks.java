package com.example.hospital_0515.scheduler;

import com.example.hospital_0515.model.Visit;
import com.example.hospital_0515.repository.VisitRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.example.hospital_0515.model.Bid;
import com.example.hospital_0515.repository.BidRepository;
import com.example.hospital_0515.repository.PatientRepository;
import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.util.PatientBookingLogger;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;
import java.time.DayOfWeek;

/**
 * ScheduledTasks类定义了定时任务，负责处理周期性出诊和竞拍号源的处理逻辑。
 */
@Component
public class ScheduledTasks {

    @Autowired
    private VisitRepository visitRepository;

    @Autowired
    private BidRepository bidRepository;

    @Autowired
    private PatientRepository patientRepository;

    /**
     * 每天午夜执行的定时任务，用于生成具体的出诊记录。
     */
    @Scheduled(cron = "0 0 0 * * ?")  // 每天午夜执行
    public void scheduleFixedRateTask() {
        System.out.println("-----");
        // 获取所有周期性出诊记录
        List<Visit> recurringVisits = visitRepository.findByRecurring(true);
        for (Visit recurringVisit : recurringVisits) {
            // 判断是否距离出诊日还有三天
            if (isThreeDaysBeforeRecurringVisitDay(recurringVisit)) {
                // 创建一个新的出诊记录
                Visit visit = new Visit();
                visit.setDepartment(recurringVisit.getDepartment());
                visit.setVisitTime(getNextVisitTime(recurringVisit));
                visit.setAvailableSlots(recurringVisit.getAvailableSlots());
                visit.setStatus("PENDING");
                visit.setDoctorName(recurringVisit.getDoctorName());
                visit.setRecurring(false);  // 生成的具体号源不再是周期性号源
                visitRepository.save(visit);
                System.out.println("----Generated a visit for " + recurringVisit.getDoctorName() + " on " + visit.getVisitTime() + " ----");
            }
        }
    }

    /**
     * 每天午夜执行的定时任务，用于处理竞拍号源的结束逻辑。
     */
    @Scheduled(cron = "0 0 0 * * ?")  // 每天午夜执行
    public void handleAuctionEnd() {
        // 获取所有状态为“APPROVED”的竞拍号源
        List<Visit> auctionVisits = visitRepository.findByStatusAndAuction("APPROVED", true);
        LocalDateTime now = LocalDateTime.now();
        System.out.println("----");

        for (Visit visit : auctionVisits) {
            // 判断是否为当前日期
            if (visit.getVisitTime().toLocalDate().equals(now.toLocalDate())) {
                // 获取当前号源的所有竞拍记录
                List<Bid> bids = bidRepository.findByVisitId(visit.getId());
                int availableSlots = visit.getAvailableSlots();

                // 按出价*信用分排序，取前availableSlots名
                List<Bid> sortedBids = bids.stream()
                        .sorted(Comparator.comparingDouble((Bid b) -> {
                            Patient patient = patientRepository.findByUsername(b.getPatientUsername());
                            return b.getBidAmount() * patient.getCreditScore();
                        }).reversed())
                        .limit(availableSlots)
                        .collect(Collectors.toList());

                // 处理每一个中标的竞拍记录
                for (Bid bid : sortedBids) {
                    Patient patient = patientRepository.findByUsername(bid.getPatientUsername());
                    visit.setAuction(false);
                    visit.getBookedBy().add(patient.getUsername());
                    visit.setAvailableSlots(visit.getAvailableSlots() - 1);
                    // 记录预定日志
                    PatientBookingLogger.logBooking("AUCTION_BOOK", patient.getUsername(), visit.getId());
                }

                // 保存更新后的号源信息
                visitRepository.save(visit);
            }
        }
    }

    /**
     * 判断是否距离周期性出诊日还有三天
     * @param recurringVisit 周期性出诊记录
     * @return 是否距离出诊日还有三天
     */
    private boolean isThreeDaysBeforeRecurringVisitDay(Visit recurringVisit) {
        DayOfWeek visitDay = recurringVisit.getRecurringDayOfWeek();
        DayOfWeek today = LocalDateTime.now().plusDays(3).getDayOfWeek();
        return visitDay == today;
    }

    /**
     * 获取下一个出诊时间
     * @param recurringVisit 周期性出诊记录
     * @return 下一个出诊时间
     */
    private LocalDateTime getNextVisitTime(Visit recurringVisit) {
        return LocalDateTime.of(LocalDateTime.now().plusDays(3).toLocalDate(), recurringVisit.getRecurringVisitTime());
    }
}
