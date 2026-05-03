package com.sitevisit.smartfieldoperations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
public class SecurityConfig {

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/",
                                "/login",
                                "/forgot-password",
                                "/reset-password",
                                "/dashboard",
                                "/members",
                                "/companies",
                                "/site-visits",
                                "/reports",
                                "/profile",
                                "/change-password",
                                "/reminders-notifications",

                                "/css/**",
                                "/js/**",
                                "/images/**",

                                "/api/auth/**",
                                "/api/companies/**",
                                "/api/members/**",
                                "/api/reports/**",
                                "/api/site-visit-reminders/**",
                                "/api/payment-reminders/**",
                                "/api/notifications/**"
                        ).permitAll()
                        .anyRequest().permitAll()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable());

        return http.build();
    }
}