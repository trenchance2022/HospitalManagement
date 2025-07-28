//SecurityConfig.java
package com.example.hospital_0515.config;

import com.example.hospital_0515.service.CustomUserDetailsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;

@Configuration // 标记这是一个配置类，Spring 会将其识别为一个配置文件
@EnableWebSecurity // 启用 Spring Security 的 Web 安全功能
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    @Autowired // 自动注入自定义的用户详情服务
    private CustomUserDetailsService userDetailsService;

    /**
     * 配置全局认证管理器
     * @param auth 认证管理构建器
     * @throws Exception 可能抛出的异常
     */
    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        // 设置用户详情服务和密码编码器
        auth.userDetailsService(userDetailsService).passwordEncoder(passwordEncoder());
    }

    /**
     * 配置 HTTP 安全
     * @param http HTTP 安全对象
     * @throws Exception 可能抛出的异常
     */
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http
                .csrf().disable() // 禁用 CSRF 防护
                .authorizeRequests() // 启用请求授权
                .antMatchers("/api/users/register/**").permitAll() // 允许所有用户访问注册相关的 URL
                .antMatchers("/login").permitAll() // 允许所有用户访问登录 URL
                .antMatchers("/change-password").hasRole("ADMIN_FIRST_LOGIN") // 仅允许第一次登录的管理员访问更改密码页面
                .antMatchers("/api/users/**").authenticated() // 需要认证的用户才能访问 /api/users 下的所有 URL
                .antMatchers("/admin.html").hasRole("ADMIN") // 仅允许管理员访问 admin.html
                .antMatchers("/doctor.html").hasRole("DOCTOR") // 仅允许医生访问 doctor.html
                .antMatchers("/patient.html").hasRole("PATIENT") // 仅允许患者访问 patient.html
                .and()
                .formLogin() // 启用表单登录
                .loginPage("/login.html") // 设置自定义登录页面
                .loginProcessingUrl("/login") // 设置处理登录请求的 URL
                .successHandler(authenticationSuccessHandler()) // 设置自定义登录成功处理
                .permitAll() // 允许所有用户访问登录页面和登录处理 URL
                .and()
                .logout().permitAll(); // 允许所有用户访问登出功能
    }

    /**
     * 定义密码编码器 Bean
     * @return 密码编码器
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 密码编码器
        return new BCryptPasswordEncoder();
    }

    /**
     * 自定义登录成功处理器 Bean
     * @return 登录成功处理器
     */
    @Bean
    public AuthenticationSuccessHandler authenticationSuccessHandler() {
        return (request, response, authentication) -> {
            // 遍历用户的权限
            authentication.getAuthorities().forEach(authority -> {
                try {
                    // 根据用户角色重定向到不同的页面
                    if (authority.getAuthority().equals("ROLE_ADMIN_FIRST_LOGIN")) {
                        response.sendRedirect("/change-password.html"); // 第一次登录的管理员重定向到更改密码页面
                    } else if (authority.getAuthority().equals("ROLE_ADMIN")) {
                        response.sendRedirect("/admin.html"); // 管理员重定向到管理员界面
                    } else if (authority.getAuthority().equals("ROLE_DOCTOR")) {
                        response.sendRedirect("/doctor.html"); // 医生重定向到医生界面
                    } else if (authority.getAuthority().equals("ROLE_PATIENT")) {
                        response.sendRedirect("/patient.html"); // 患者重定向到患者界面
                    }
                } catch (Exception e) {
                    e.printStackTrace(); // 打印异常堆栈信息
                }
            });
        };
    }
}
