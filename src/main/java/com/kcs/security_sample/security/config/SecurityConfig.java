package com.kcs.security_sample.security.config;

import com.kcs.security_sample.security.filter.JsonFilter;
import com.kcs.security_sample.security.filter.JwtAuthenticationFilter;
import com.kcs.security_sample.security.filter.MultipartFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
@EnableMethodSecurity(securedEnabled = true)
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MultipartFilter multipartFilter;
    private final JsonFilter jsonFilter;

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new PasswordEncoder() {
            @Override
            public String encode(CharSequence rawPassword) {
                return rawPassword.toString();
            }

            @Override
            public boolean matches(CharSequence rawPassword, String encodedPassword) {
                return rawPassword.toString().equals(encodedPassword);
            }
        };
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests ->
                        authorizeRequests
                                .requestMatchers(
                                        "/login", "/api/v1/login", "/api/v1/submit/danger",
                                        "/api/v1/upload/file", "/api/v1/submit/create", "/api/v1/submit/formdata",
                                        "/api/v1/submit/delete", "/api/v1/jsonfile/upload", "/api/v1/multipart/upload")
                                .permitAll()
                                .requestMatchers("/admin").hasRole("ADMIN")
                                .requestMatchers("/user").hasAnyRole("ADMIN", "USER", "MANAGER")
                                .requestMatchers("/settlement").hasAnyRole("MANAGER", "ADMIN")
                                .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jsonFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(multipartFilter, JsonFilter.class);
//                .addFilterAfter(jsonFilter, JwtAuthenticationFilter.class)
              //  .addFilterAfter(multipartFilter, JsonFilter.class);

        return http.build();
    }
}