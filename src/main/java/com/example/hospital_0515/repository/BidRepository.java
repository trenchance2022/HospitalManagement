package com.example.hospital_0515.repository;

import com.example.hospital_0515.model.Bid;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * BidRepository接口继承了JpaRepository，用于管理Bid实体的CRUD操作。
 */
public interface BidRepository extends JpaRepository<Bid, Long> {

    /**
     * 根据出诊ID查找竞拍记录
     * @param visitId 出诊ID
     * @return 返回对应出诊ID的竞拍记录列表
     */
    List<Bid> findByVisitId(Long visitId);

    /**
     * 根据患者用户名查找竞拍记录
     * @param patientUsername 患者用户名
     * @return 返回对应患者用户名的竞拍记录列表
     */
    List<Bid> findByPatientUsername(String patientUsername);

    /**
     * 根据出诊ID查找并按竞拍金额降序排列竞拍记录
     * @param visitId 出诊ID
     * @return 返回按竞拍金额降序排列的竞拍记录列表
     */
    @Query("SELECT b FROM Bid b WHERE b.visitId = :visitId ORDER BY b.bidAmount DESC")
    List<Bid> findByVisitIdOrderByBidAmountDesc(@Param("visitId") Long visitId);
}

