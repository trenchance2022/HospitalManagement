package com.example.hospital_0515.service;

import com.example.hospital_0515.model.Admin;
import com.example.hospital_0515.model.Doctor;
import com.example.hospital_0515.model.Patient;
import com.example.hospital_0515.repository.AdminRepository;
import com.example.hospital_0515.repository.DoctorRepository;
import com.example.hospital_0515.repository.PatientRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

/**
 * CustomUserDetailsService 实现 UserDetailsService 接口
 * 用于根据用户名加载用户信息，并为 Spring Security 提供用户身份验证功能。
 */
@Service
public class CustomUserDetailsService implements UserDetailsService {

    @Autowired
    private PatientRepository patientRepository;

    @Autowired
    private DoctorRepository doctorRepository;

    @Autowired
    private AdminRepository adminRepository;

    /**
     * 根据用户名加载用户信息
     * @param username 用户名
     * @return UserDetails 用户详细信息
     * @throws UsernameNotFoundException 用户名未找到异常
     */
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        // 查找管理员账户
        Admin admin = adminRepository.findByUsername(username);
        if (admin != null && "APPROVED".equals(admin.getStatus())) {
            if ("admin".equals(username) && admin.isFirstLogin()) {
                // 如果是第一次登录，分配 ROLE_ADMIN_FIRST_LOGIN 权限
                List<GrantedAuthority> authorities = getAuthorities("ROLE_ADMIN_FIRST_LOGIN");
                return new User(admin.getUsername(), admin.getPassword(), authorities);
            }
            // 分配 ROLE_ADMIN 权限
            return new User(admin.getUsername(), admin.getPassword(), getAuthorities("ROLE_ADMIN"));
        }

        // 查找医生账户
        Doctor doctor = doctorRepository.findByUsername(username);
        if (doctor != null && "APPROVED".equals(doctor.getStatus())) {
            // 分配 ROLE_DOCTOR 权限
            return new User(doctor.getUsername(), doctor.getPassword(), getAuthorities("ROLE_DOCTOR"));
        }

        // 查找患者账户
        Patient patient = patientRepository.findByUsername(username);
        if (patient != null && "APPROVED".equals(patient.getStatus())) {
            // 分配 ROLE_PATIENT 权限
            return new User(patient.getUsername(), patient.getPassword(), getAuthorities("ROLE_PATIENT"));
        }

        // 用户名未找到，抛出异常
        throw new UsernameNotFoundException("用户名或密码错误");
    }

    /**
     * 获取用户权限
     * @param role 用户角色
     * @return List<GrantedAuthority> 用户权限列表
     */
    private List<GrantedAuthority> getAuthorities(String role) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        // 添加指定角色的权限
        authorities.add(new SimpleGrantedAuthority(role));
        return authorities;
    }
}
