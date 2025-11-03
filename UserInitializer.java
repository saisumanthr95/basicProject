package com.example.demo.config;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class UserInitializer {

    @Bean
    CommandLineRunner initUsers(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            // 🔹 Ensure Admin user exists
            if (userRepository.findByUsername("adminuser").isEmpty()) {
                User admin = new User();
                admin.setUsername("adminuser");
                admin.setPassword(passwordEncoder.encode("adminpass"));
                admin.setRole("ROLE_ADMIN");

                // ✅ Required new fields
                admin.setEmail("admin@example.com");
                admin.setFirstName("System");
                admin.setLastName("Administrator");
                admin.setVerified(true);

                userRepository.save(admin);
                System.out.println("✅ Admin user created: username=adminuser, password=adminpass");
            } else {
                System.out.println("ℹ️ Admin user already exists, skipping creation.");
            }

            // 🔹 Ensure Default Test user exists
            if (userRepository.findByUsername("jwtuser").isEmpty()) {
                User testUser = new User();
                testUser.setUsername("jwtuser");
                testUser.setPassword(passwordEncoder.encode("jwtpass"));
                testUser.setRole("ROLE_USER");

                // ✅ Required new fields
                testUser.setEmail("jwtuser@example.com");
                testUser.setFirstName("JWT");
                testUser.setLastName("User");
                testUser.setVerified(true);

                userRepository.save(testUser);
                System.out.println("✅ Test user created: username=jwtuser, password=jwtpass");
            } else {
                System.out.println("ℹ️ Test user already exists, skipping creation.");
            }
        };
    }
}
