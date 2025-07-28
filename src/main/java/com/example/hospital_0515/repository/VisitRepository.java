package com.example.hospital_0515.repository;

import com.example.hospital_0515.model.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;

/**
 * VisitRepository接口继承了JpaRepository，用于管理Visit实体的CRUD操作。
 */
public interface VisitRepository extends JpaRepository<Visit, Long> {

    /**
     * 根据状态查找出诊记录
     * @param status 出诊状态
     * @return 返回对应状态的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.status = :status")
    List<Visit> findByStatus(@Param("status") String status);

    /**
     * 根据状态和是否为竞拍号源查找出诊记录
     * @param status 出诊状态
     * @param auction 是否为竞拍号源
     * @return 返回对应状态和竞拍条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.status = :status AND v.auction = :auction")
    List<Visit> findByStatusAndAuction(@Param("status") String status, @Param("auction") boolean auction);

    /**
     * 根据医生用户名查找竞拍号源的出诊记录
     * @param doctorName 医生用户名
     * @return 返回对应医生的竞拍号源出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.doctorName = :doctorName AND v.auction = true")
    List<Visit> findByDoctorNameAndAuction(@Param("doctorName") String doctorName);

    /**
     * 根据医生用户名查找普通出诊记录
     * @param doctorName 医生用户名
     * @return 返回对应医生的普通出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.doctorName = :doctorName AND v.auction = false AND v.recurring = false")
    List<Visit> findByDoctorNameAndNormal(@Param("doctorName") String doctorName);

    /**
     * 根据医生用户名、时间范围、状态和是否为竞拍号源查找出诊记录
     * @param doctorName 医生用户名
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @param status 出诊状态
     * @param auction 是否为竞拍号源
     * @return 返回对应条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.doctorName = :doctorName AND v.visitTime BETWEEN :startDate AND :endDate AND v.status = :status AND v.auction = :auction")
    List<Visit> findByDoctorNameAndVisitTimeBetweenAndStatusAndAuction(@Param("doctorName") String doctorName,
                                                                       @Param("startDate") LocalDateTime startDate,
                                                                       @Param("endDate") LocalDateTime endDate,
                                                                       @Param("status") String status,
                                                                       @Param("auction") boolean auction);

    /**
     * 根据预约患者用户名查找出诊记录
     * @param bookedBy 预约患者用户名
     * @return 返回包含预约患者的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE :bookedBy MEMBER OF v.bookedBy")
    List<Visit> findByBookedByContaining(@Param("bookedBy") String bookedBy);

    /**
     * 根据预约患者用户名和时间范围查找出诊记录
     * @param bookedBy 预约患者用户名
     * @param startDate 开始时间
     * @param endDate 结束时间
     * @return 返回包含预约患者且在指定时间范围内的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE :bookedBy MEMBER OF v.bookedBy AND v.visitTime BETWEEN :startDate AND :endDate")
    List<Visit> findByBookedByContainingAndVisitTimeBetween(@Param("bookedBy") String bookedBy,
                                                            @Param("startDate") LocalDateTime startDate,
                                                            @Param("endDate") LocalDateTime endDate);

    /**
     * 根据医生用户名、是否为周期性出诊和是否为竞拍号源查找出诊记录
     * @param doctorName 医生用户名
     * @param recurring 是否为周期性出诊
     * @param auction 是否为竞拍号源
     * @return 返回对应条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.doctorName = :doctorName AND v.recurring = :recurring AND v.auction = :auction")
    List<Visit> findByDoctorNameAndRecurringAndAuction(@Param("doctorName") String doctorName,
                                                       @Param("recurring") boolean recurring,
                                                       @Param("auction") boolean auction);

    /**
     * 根据是否为周期性出诊、状态和是否为竞拍号源查找出诊记录
     * @param recurring 是否为周期性出诊
     * @param status 出诊状态
     * @param auction 是否为竞拍号源
     * @return 返回对应条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.recurring = :recurring AND v.status = :status AND v.auction = :auction")
    List<Visit> findByRecurringAndStatusAndAuction(@Param("recurring") boolean recurring,
                                                   @Param("status") String status,
                                                   @Param("auction") boolean auction);

    /**
     * 根据是否为周期性出诊查找出诊记录
     * @param recurring 是否为周期性出诊
     * @return 返回对应条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.recurring = :recurring")
    List<Visit> findByRecurring(@Param("recurring") boolean recurring);

    /**
     * 根据医生用户名查找出诊记录
     * @param doctorName 医生用户名
     * @return 返回对应医生的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.doctorName = :doctorName")
    List<Visit> findByDoctorName(@Param("doctorName") String doctorName);

    /**
     * 根据医生用户名和预约患者用户名查找出诊记录
     * @param doctorName 医生用户名
     * @param bookedBy 预约患者用户名
     * @return 返回对应医生和预约患者的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.doctorName = :doctorName AND :bookedBy MEMBER OF v.bookedBy")
    List<Visit> findByDoctorNameAndBookedByContaining(@Param("doctorName") String doctorName,
                                                      @Param("bookedBy") String bookedBy);

    /**
     * 查找可用的出诊记录，根据多个条件过滤，包括状态、科室、医生和时间范围
     * @param department 科室
     * @param doctorName 医生用户名
     * @param start 开始时间
     * @param end 结束时间
     * @param auction 是否为竞拍号源
     * @return 返回符合条件的可用出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.status = 'APPROVED' AND v.availableSlots > 0 AND v.auction = :auction AND " +
            "(v.department = :department OR :department IS NULL OR :department = '') AND " +
            "(v.doctorName = :doctorName OR :doctorName IS NULL OR :doctorName = '') AND " +
            "v.visitTime BETWEEN :start AND :end")
    List<Visit> findAvailableVisits(@Param("department") String department,
                                    @Param("doctorName") String doctorName,
                                    @Param("start") LocalDateTime start,
                                    @Param("end") LocalDateTime end,
                                    @Param("auction") boolean auction);

    /**
     * 根据科室和时间查找出诊记录
     * @param department 科室
     * @param visitTime 出诊时间
     * @return 返回符合科室和时间条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.department = :department AND v.visitTime > :visitTime AND v.auction = false")
    List<Visit> findByDepartmentAndVisitTimeAfter(@Param("department") String department,
                                                  @Param("visitTime") LocalDateTime visitTime);

    /**
     * 根据多个科室和时间查找出诊记录
     * @param departments 科室集合
     * @param visitTime 出诊时间
     * @return 返回符合科室集合和时间条件的出诊记录列表
     */
    @Query("SELECT v FROM Visit v WHERE v.department IN :departments AND v.visitTime > :visitTime AND v.auction = false")
    List<Visit> findByDepartmentInAndVisitTimeAfter(@Param("departments") Set<String> departments,
                                                    @Param("visitTime") LocalDateTime visitTime);

    /**
     * 根据时间查找出诊记录，并按时间升序排序
     * @param visitTime 出诊时间
     * @return 返回符合时间条件的出诊记录列表，并按时间升序排序
     */
    @Query("SELECT v FROM Visit v WHERE v.visitTime > :visitTime AND v.auction = false ORDER BY v.visitTime ASC")
    List<Visit> findByVisitTimeAfterOrderByVisitTimeAsc(@Param("visitTime") LocalDateTime visitTime);
}

