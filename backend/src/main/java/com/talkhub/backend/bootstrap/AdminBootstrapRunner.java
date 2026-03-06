package com.talkhub.backend.bootstrap;

import com.talkhub.backend.config.AppProperties;
import com.talkhub.backend.domain.user.UserEntity;
import com.talkhub.backend.domain.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@Order(Ordered.HIGHEST_PRECEDENCE + 100)
public class AdminBootstrapRunner implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminBootstrapRunner.class);

    private final AppProperties appProperties;
    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public AdminBootstrapRunner(AppProperties appProperties, UserRepository userRepository) {
        this.appProperties = appProperties;
        this.userRepository = userRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        AppProperties.Admin admin = appProperties.getAdmin();
        String username = admin.getUsername().trim();
        String rawPassword = admin.getPassword();

        UserEntity user = userRepository.findByUsername(username).orElseGet(() -> {
            UserEntity created = new UserEntity();
            created.setUsername(username);
            created.setNickname(admin.getNickname());
            created.setStatus("ACTIVE");
            return created;
        });

        boolean changed = false;
        if (user.getPasswordHash() == null || !passwordEncoder.matches(rawPassword, user.getPasswordHash())) {
            user.setPasswordHash(passwordEncoder.encode(rawPassword));
            changed = true;
        }

        if (user.getNickname() == null || user.getNickname().isBlank()) {
            user.setNickname(admin.getNickname());
            changed = true;
        }

        if (!"ACTIVE".equals(user.getStatus())) {
            user.setStatus("ACTIVE");
            changed = true;
        }

        userRepository.save(user);
        if (changed) {
            log.info("Admin user '{}' initialized or updated from environment.", username);
        } else {
            log.info("Admin user '{}' already in sync with environment.", username);
        }
    }
}
