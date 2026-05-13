package com.sitevisit.smartfieldoperations.controller;

import com.sitevisit.smartfieldoperations.dto.*;
import com.sitevisit.smartfieldoperations.entity.User;
import com.sitevisit.smartfieldoperations.repository.UserRepository;
import com.sitevisit.smartfieldoperations.service.AuthService;

import jakarta.servlet.http.HttpSession;

import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
@CrossOrigin(origins = "*")
public class AuthController {

    private final AuthService authService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthController(AuthService authService,
                          UserRepository userRepository,
                          PasswordEncoder passwordEncoder) {

        this.authService = authService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    // ================= LOGIN =================

    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request,
                                               HttpSession session) {

        LoginResponse response = authService.login(request);

        if (response.isSuccess()) {
            session.setAttribute("loggedInUserEmail", request.getEmail().trim());
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    // ================= FORGOT PASSWORD =================

    @PostMapping("/forgot-password")
    public ResponseEntity<ApiResponse> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {

        ApiResponse response = authService.forgotPassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    // ================= RESET PASSWORD =================

    @PostMapping("/reset-password")
    public ResponseEntity<ApiResponse> resetPassword(
            @RequestBody ResetPasswordRequest request) {

        ApiResponse response = authService.resetPassword(request);

        if (response.isSuccess()) {
            return ResponseEntity.ok(response);
        }

        return ResponseEntity.badRequest().body(response);
    }

    // ================= CHANGE PASSWORD =================

    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request,
                                            HttpSession session) {

        String email = (String) session.getAttribute("loggedInUserEmail");

        if (email == null) {
            return ResponseEntity.status(401).body(Map.of(
                    "success", false,
                    "message", "You must be logged in to change your password."
            ));
        }

        Optional<User> optionalUser = userRepository.findByEmail(email);

        if (optionalUser.isEmpty()) {
            return ResponseEntity.status(404).body(Map.of(
                    "success", false,
                    "message", "User account not found."
            ));
        }

        User user = optionalUser.get();

        if (request.getCurrentPassword() == null ||
                request.getCurrentPassword().isBlank()) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Current password is required."
            ));
        }

        if (request.getNewPassword() == null ||
                request.getNewPassword().isBlank()) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "New password is required."
            ));
        }

        if (request.getConfirmPassword() == null ||
                request.getConfirmPassword().isBlank()) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Please confirm your new password."
            ));
        }

        if (!passwordEncoder.matches(
                request.getCurrentPassword(),
                user.getPassword())) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "Current password is incorrect."
            ));
        }

        if (request.getNewPassword().length() < 8) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "New password must be at least 8 characters long."
            ));
        }

        if (passwordEncoder.matches(
                request.getNewPassword(),
                user.getPassword())) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "New password must be different from your current password."
            ));
        }

        if (!request.getNewPassword()
                .equals(request.getConfirmPassword())) {

            return ResponseEntity.badRequest().body(Map.of(
                    "success", false,
                    "message", "New password and confirm password do not match."
            ));
        }

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));

        userRepository.save(user);

        session.invalidate();

        return ResponseEntity.ok(Map.of(
                "success", true,
                "message", "Password changed successfully. Please log in again."
        ));
    }
}