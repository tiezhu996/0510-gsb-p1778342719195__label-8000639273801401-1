package com.cardmanager.config;

import com.cardmanager.entity.SysUser;
import com.cardmanager.repository.SysUserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final SysUserRepository userRepository;
    private final JdbcTemplate jdbcTemplate;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    @Override
    public void run(String... args) {
        migrateOldUniqueIndex();

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

    private void migrateOldUniqueIndex() {
        try {
            List<Map<String, Object>> indexes = jdbcTemplate.queryForList(
                "SHOW INDEX FROM sys_user WHERE Non_unique = 0"
            );

            Map<String, Integer> indexColumnCount = new java.util.HashMap<>();
            Map<String, String> indexFirstColumn = new java.util.HashMap<>();

            for (Map<String, Object> index : indexes) {
                String keyName = (String) index.get("Key_name");
                String columnName = (String) index.get("Column_name");
                Integer seqInIndex = ((Number) index.get("Seq_in_index")).intValue();

                indexColumnCount.merge(keyName, 1, Integer::sum);
                if (seqInIndex == 1) {
                    indexFirstColumn.put(keyName, columnName);
                }
            }

            for (Map.Entry<String, Integer> entry : indexColumnCount.entrySet()) {
                String keyName = entry.getKey();
                Integer columnCount = entry.getValue();
                String firstColumn = indexFirstColumn.get(keyName);

                if (columnCount == 1 && "username".equals(firstColumn) && !"PRIMARY".equals(keyName)) {
                    log.info("Found old single-column unique index on username: {}, dropping...", keyName);
                    jdbcTemplate.execute("ALTER TABLE sys_user DROP INDEX " + keyName);
                    log.info("Successfully dropped old unique index: {}", keyName);
                }
            }
        } catch (Exception e) {
            log.warn("Failed to check/migrate old unique index, this may be normal for new databases: {}", e.getMessage());
        }
    }
}
