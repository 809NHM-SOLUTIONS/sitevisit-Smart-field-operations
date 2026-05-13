package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.dto.ApiResponse;
import com.sitevisit.smartfieldoperations.dto.ForgotPasswordRequest;
import com.sitevisit.smartfieldoperations.dto.LoginRequest;
import com.sitevisit.smartfieldoperations.dto.LoginResponse;
import com.sitevisit.smartfieldoperations.dto.ResetPasswordRequest;
import com.sitevisit.smartfieldoperations.entity.User;
import com.sitevisit.smartfieldoperations.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JavaMailSender mailSender) {

        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public LoginResponse login(LoginRequest request) {
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return new LoginResponse(false, "Email is required", null, null);
        }

        if (request.getPassword() == null || request.getPassword().trim().isEmpty()) {
            return new LoginResponse(false, "Password is required", null, null);
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail().trim());

        if (userOptional.isEmpty()) {
            return new LoginResponse(false, "Invalid email or password", null, null);
        }

        User user = userOptional.get();

        // Uses the same saved password until the user changes/resets it
        if (!passwordEncoder.matches(request.getPassword().trim(), user.getPassword())) {
            return new LoginResponse(false, "Invalid email or password", null, null);
        }

        return new LoginResponse(
                true,
                "Login successful",
                user.getFullName(),
                user.getRole()
        );
    }

    public ApiResponse forgotPassword(ForgotPasswordRequest request) {

        if (request == null ||
                request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            return new ApiResponse(false, "Email is required");
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(request.getEmail().trim());

        if (optionalUser.isEmpty()) {
            return new ApiResponse(false,
                    "No account found with that email");
        }

        User user = optionalUser.get();

        // Generate OTP
        String otp = String.valueOf(
                (int)((Math.random() * 900000) + 100000)
        );

        // Save OTP
        user.setOtp(otp);
        user.setOtpExpiry(LocalDateTime.now().plusMinutes(10));

        userRepository.save(user);

        try {

            // Create Email
            SimpleMailMessage message = new SimpleMailMessage();

            message.setTo(user.getEmail());

            message.setSubject("Password Reset OTP");

            message.setText(
                    "Hello " + user.getFullName() + ",\n\n" +
                            "Your OTP is: " + otp + "\n\n" +
                            "This OTP expires in 10 minutes.\n\n" +
                            "SiteVisit System"
            );

            // SEND EMAIL
            mailSender.send(message);

            System.out.println("OTP SENT SUCCESSFULLY");

            return new ApiResponse(
                    true,
                    "OTP sent successfully"
            );

        } catch (Exception e) {

            e.printStackTrace();

            return new ApiResponse(
                    false,
                    "Failed to send OTP email"
            );
        }
    }

    public ApiResponse resetPassword(ResetPasswordRequest request) {

        if (request == null ||
                request.getEmail() == null ||
                request.getEmail().trim().isEmpty()) {

            return new ApiResponse(false, "Email is required");
        }

        if (request.getOtp() == null ||
                request.getOtp().trim().isEmpty()) {

            return new ApiResponse(false, "OTP is required");
        }

        if (request.getNewPassword() == null ||
                request.getNewPassword().trim().isEmpty()) {

            return new ApiResponse(false, "New password is required");
        }

        Optional<User> optionalUser =
                userRepository.findByEmail(request.getEmail().trim());

        if (optionalUser.isEmpty()) {
            return new ApiResponse(false, "User not found");
        }

        User user = optionalUser.get();

        // Validate OTP
        if (user.getOtp() == null ||
                !user.getOtp().equals(request.getOtp().trim())) {

            return new ApiResponse(false, "Invalid OTP");
        }

        // Check OTP expiry
        if (user.getOtpExpiry().isBefore(LocalDateTime.now())) {

            return new ApiResponse(false, "OTP has expired");
        }

        String newPassword = request.getNewPassword().trim();

        if (newPassword.length() < 8) {
            return new ApiResponse(false,
                    "Password must be at least 8 characters");
        }

        // Prevent same password reuse
        if (passwordEncoder.matches(newPassword, user.getPassword())) {

            return new ApiResponse(false,
                    "New password must be different from current password");
        }

        // Save new password
        user.setPassword(passwordEncoder.encode(newPassword));

        // Clear OTP
        user.setOtp(null);
        user.setOtpExpiry(null);

        userRepository.save(user);

        return new ApiResponse(
                true,
                "Password reset successful"
        );
    }
}