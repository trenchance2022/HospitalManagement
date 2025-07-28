//AdminRepository.java
package com.example.hospital_0515.repository;

import com.example.hospital_0515.model.Admin;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * AdminRepository接口继承了JpaRepository，用于管理Admin实体的CRUD操作。
 */
public interface AdminRepository extends JpaRepository<Admin, Long> {

    /**
     * 根据用户名查找管理员
     * @param username 管理员用户名
     * @return 返回对应用户名的管理员对象
     */
    Admin findByUsername(String username);

    /**
     * 根据状态查找管理员列表
     * @param status 管理员状态
     * @return 返回对应状态的管理员列表
     */
    List<Admin> findByStatus(String status);

    /**
     * 判断用户名是否存在
     * @param username 管理员用户名
     * @return 返回布尔值，表示用户名是否存在
     */
    @Query("SELECT CASE WHEN COUNT(a) > 0 THEN true ELSE false END FROM Admin a WHERE a.username = :username")
    boolean existsByUsername(@Param("username") String username);
}


