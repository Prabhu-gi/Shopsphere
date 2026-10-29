package com.shopsphere.common.config;

import com.shopsphere.user.entity.Role;
import com.shopsphere.user.entity.User;
import com.shopsphere.user.entity.UserStatus;
import com.shopsphere.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@Profile("!test")
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (!userRepository.existsByEmail("admin@shopsphere.com")) {
            User admin = User.builder()
                    .email("admin@shopsphere.com")
                    .passwordHash(passwordEncoder.encode("Admin@123"))
                    .firstName("Platform")
                    .lastName("Admin")
                    .phone("+1234567890")
                    .role(Role.ADMIN)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(admin);
            log.info("Initialized default admin user: admin@shopsphere.com / Admin@123");
        }

        if (!userRepository.existsByEmail("customer@shopsphere.com")) {
            User customer = User.builder()
                    .email("customer@shopsphere.com")
                    .passwordHash(passwordEncoder.encode("Password@123"))
                    .firstName("John")
                    .lastName("Doe")
                    .phone("+1987654321")
                    .role(Role.CUSTOMER)
                    .status(UserStatus.ACTIVE)
                    .build();
            userRepository.save(customer);
            log.info("Initialized default customer user: customer@shopsphere.com / Password@123");
        }
    }
}
