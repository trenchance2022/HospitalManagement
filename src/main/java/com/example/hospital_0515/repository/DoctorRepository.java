//DoctorRepository.java
package com.example.hospital_0515.repository;

import com.example.hospital_0515.model.Doctor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * DoctorRepository接口继承了JpaRepository，用于管理Doctor实体的CRUD操作。
 */
public interface DoctorRepository extends JpaRepository<Doctor, Long> {

    /**
     * 根据用户名查找医生
     * @param username 医生用户名
     * @return 返回对应用户名的医生实体
     */
    Doctor findByUsername(String username);

    /**
     * 根据状态查找医生
     * @param status 医生状态
     * @return 返回对应状态的医生列表
     */
    List<Doctor> findByStatus(String status);

    /**
     * 检查指定用户名的医生是否存在
     * @param username 医生用户名
     * @return 如果存在则返回true，否则返回false
     */
    @Query("SELECT CASE WHEN COUNT(d) > 0 THEN true ELSE false END FROM Doctor d WHERE d.username = :username")
    boolean existsByUsername(@Param("username") String username);
}


