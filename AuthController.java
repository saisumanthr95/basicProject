package com.example.demo.controller;

import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import com.example.demo.security.JwtUtil;
import com.example.demo.service.EmailService;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final EmailService emailService;

    @Autowired
    public AuthController(UserRepository userRepository,
                          PasswordEncoder passwordEncoder,
                          JwtUtil jwtUtil,
                          EmailService emailService) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
        this.emailService = emailService;
    }

    // -----------------------------
    // 1️⃣ REGISTER (Send verification email)
    // -----------------------------
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody Map<String, String> req) {
        String firstName = req.get("firstName");
        String lastName = req.get("lastName");
        String email = req.get("email");

        if (userRepository.findByEmail(email).isPresent()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email already registered"));
        }

        // Create unverified user with random token
        User user = new User();
        user.setUsername(email); // username = email
        user.setFirstName(firstName);
        user.setLastName(lastName);
        user.setEmail(email);
        user.setRole("ROLE_USER");
        user.setVerified(false);
        user.setVerificationToken(UUID.randomUUID().toString());
        userRepository.save(user);

        // ✅ Send setup password email
        String setupLink = "http://localhost:4200/setup-password?token=" + user.getVerificationToken();
        emailService.sendPasswordSetupEmail(email, firstName, setupLink);

        String subject = "Set up your password";
        String message = "Hi " + firstName + ",\n\nPlease set your password using the link below:\n" + setupLink + "\n\nThank you!";

        emailService.sendEmail(email, subject, message);

        return ResponseEntity.ok(Map.of(
                "message", "Verification email sent. Please check your inbox.",
                "email", email
        ));
    }

    // -----------------------------
    // 2️⃣ VERIFY TOKEN (optional)
    // -----------------------------
    @GetMapping("/verify")
    public ResponseEntity<?> verifyToken(@RequestParam("token") String token) {
        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }

        return ResponseEntity.ok(Map.of("message", "Valid token", "email", userOpt.get().getEmail()));
    }

    // -----------------------------
    // 3️⃣ SET PASSWORD
    // -----------------------------
    @PostMapping("/set-password")
    public ResponseEntity<?> setPassword(@RequestBody Map<String, String> req) {
        String token = req.get("token");
        String password = req.get("password");

        Optional<User> userOpt = userRepository.findByVerificationToken(token);
        if (userOpt.isEmpty()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Invalid or expired token"));
        }

        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(password));
        user.setVerified(true);
        user.setVerificationToken(null); // clear token
        userRepository.save(user);

        return ResponseEntity.ok(Map.of("message", "Password set successfully. You can now log in."));
    }

    // -----------------------------
    // 4️⃣ LOGIN (only verified users)
    // -----------------------------
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody Map<String, String> req) {
        String username = req.get("username");
        String password = req.get("password");

        Optional<User> userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) {
            return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
        }

        User user = userOpt.get();

        if (!user.isVerified()) {
            return ResponseEntity.status(403).body(Map.of("error", "Please verify your email before logging in"));
        }

        if (passwordEncoder.matches(password, user.getPassword())) {
            String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
            return ResponseEntity.ok(Map.of(
                    "token", token,
                    "role", user.getRole(),
                    "username", user.getUsername()
            ));
        }

        return ResponseEntity.status(401).body(Map.of("error", "Invalid credentials"));
    }
}
