package com.cardmanager.config;

import com.cardmanager.entity.SysUser;
import com.cardmanager.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        if (!userRepository.existsByUsername("admin")) {
            SysUser admin = new SysUser();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("123456"));
            admin.setRealName("系统管理员");
            admin.setRole("admin");
            admin.setStatus(0);
            userRepository.save(admin);
            log.info("Initialized admin user");
        }

        if (!userRepository.existsByUsername("operator")) {
            SysUser operator = new SysUser();
            operator.setUsername("operator");
            operator.setPassword(passwordEncoder.encode("123456"));
            operator.setRealName("测试操作员");
            operator.setRole("operator");
            operator.setStatus(0);
            userRepository.save(operator);
            log.info("Initialized operator user");
        }
    }
}
