package com.sitevisit.smartfieldoperations.service;

import com.sitevisit.smartfieldoperations.dto.ApiResponse;
import com.sitevisit.smartfieldoperations.dto.ForgotPasswordRequest;
import com.sitevisit.smartfieldoperations.dto.LoginRequest;
import com.sitevisit.smartfieldoperations.dto.LoginResponse;
import com.sitevisit.smartfieldoperations.dto.ResetPasswordRequest;
import com.sitevisit.smartfieldoperations.entity.PasswordResetToken;
import com.sitevisit.smartfieldoperations.entity.User;
import com.sitevisit.smartfieldoperations.repository.PasswordResetTokenRepository;
import com.sitevisit.smartfieldoperations.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(UserRepository userRepository,
                       PasswordResetTokenRepository passwordResetTokenRepository,
                       PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.passwordEncoder = passwordEncoder;
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
        if (request == null || request.getEmail() == null || request.getEmail().trim().isEmpty()) {
            return new ApiResponse(false, "Email is required");
        }

        Optional<User> userOptional = userRepository.findByEmail(request.getEmail().trim());

        if (userOptional.isPresent()) {
            User user = userOptional.get();

            passwordResetTokenRepository.findByUser(user)
                    .ifPresent(passwordResetTokenRepository::delete);

            String token = UUID.randomUUID().toString();
            LocalDateTime expiryDate = LocalDateTime.now().plusMinutes(15);

            PasswordResetToken resetToken = new PasswordResetToken();
            resetToken.setToken(token);
            resetToken.setUser(user);
            resetToken.setExpiryDate(expiryDate);

            passwordResetTokenRepository.save(resetToken);

            return new ApiResponse(
                    true,
                    "If the account exists, a reset token has been generated.",
                    token
            );
        }

        return new ApiResponse(
                true,
                "If the account exists, a reset token has been generated."
        );
    }

    public ApiResponse resetPassword(ResetPasswordRequest request) {
        if (request == null || request.getToken() == null || request.getToken().trim().isEmpty()) {
            return new ApiResponse(false, "Reset token is required");
        }

        if (request.getNewPassword() == null || request.getNewPassword().trim().isEmpty()) {
            return new ApiResponse(false, "New password is required");
        }

        String newPassword = request.getNewPassword().trim();

        if (newPassword.length() < 8) {
            return new ApiResponse(false, "New password must be at least 8 characters long");
        }

        Optional<PasswordResetToken> tokenOptional =
                passwordResetTokenRepository.findByToken(request.getToken().trim());

        if (tokenOptional.isEmpty()) {
            return new ApiResponse(false, "Invalid or expired token");
        }

        PasswordResetToken resetToken = tokenOptional.get();

        if (resetToken.isExpired()) {
            passwordResetTokenRepository.delete(resetToken);
            return new ApiResponse(false, "Invalid or expired token");
        }

        User user = resetToken.getUser();

        // Prevent reusing the same password during reset
        if (passwordEncoder.matches(newPassword, user.getPassword())) {
            return new ApiResponse(false, "New password must be different from the current password");
        }

        // Password only changes here
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        passwordResetTokenRepository.delete(resetToken);

        return new ApiResponse(true, "Password reset successful. Please log in using your new password.");
    }
}