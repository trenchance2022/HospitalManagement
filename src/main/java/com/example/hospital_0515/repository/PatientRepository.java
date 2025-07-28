//PatientRepository.java
package com.example.hospital_0515.repository;

import com.example.hospital_0515.model.Patient;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Set;

/**
 * PatientRepository接口继承了JpaRepository，用于管理Patient实体的CRUD操作。
 */
public interface PatientRepository extends JpaRepository<Patient, Long> {

    /**
     * 根据用户名查找患者
     * @param username 患者用户名
     * @return 返回对应用户名的患者实体
     */
    Patient findByUsername(String username);

    /**
     * 根据状态查找患者
     * @param status 患者状态
     * @return 返回对应状态的患者列表
     */
    List<Patient> findByStatus(String status);

    /**
     * 检查指定用户名的患者是否存在
     * @param username 患者用户名
     * @return 如果存在则返回true，否则返回false
     */
    @Query("SELECT CASE WHEN COUNT(p) > 0 THEN true ELSE false END FROM Patient p WHERE p.username = :username")
    boolean existsByUsername(@Param("username") String username);

    /**
     * 根据一组用户名查找患者
     * @param usernames 患者用户名集合
     * @return 返回对应用户名集合的患者列表
     */
    @Query("SELECT p FROM Patient p WHERE p.username IN :usernames")
    List<Patient> findByUsernameIn(@Param("usernames") Set<String> usernames);
}
