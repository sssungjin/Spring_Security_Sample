package com.kcs.security_sample.security.config;

import com.kcs.security_sample.dto.response.PermissionDto;
import com.kcs.security_sample.security.filter.JsonFilter;
import com.kcs.security_sample.security.filter.JwtAuthenticationFilter;
import com.kcs.security_sample.security.filter.MultipartFilter;
import com.kcs.security_sample.security.service.CustomUserDetailService;
import com.kcs.security_sample.security.service.JwtService;
import com.kcs.security_sample.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.context.DelegatingSecurityContextRepository;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.RequestAttributeSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

import java.util.*;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class SecurityConfig {
    private final CustomUserDetailService customUserDetailService;
    private final JwtService jwtService;
    private final MultipartFilter multipartFilter;
    private final JsonFilter jsonFilter;
    private final PermissionService permissionService;

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(customUserDetailService, jwtService, securityContextRepository());
    }

    // Password encoder for testing purposes (not bcrypt)
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

    // Security filter chain
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        // Get all permissions from DB
        List<PermissionDto> permissions = permissionService.getAllPermissions();

        Map<String, Set<String>> urlRoles = new HashMap<>();
        permissions.forEach(permission -> {
            urlRoles.computeIfAbsent(permission.url(), k -> new HashSet<>())
                    .add(permission.role().name());
        });

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            // Permit all requests to login, submit, file upload
                            .requestMatchers(
                                    "/login", "/api/v1/login",
                                    "/api/v1/submit/danger", "/api/v1/submit/formdata",
                                    "/api/v1/jsonfile/upload", "/api/v1/multipart/upload",
                                    "/api/v1/submit/total", "/api/v1/submit/file/*")
                            .permitAll();
                    // Set up permissions for each URL, ROLE from DB
                    urlRoles.forEach((url, roles) -> {
                        authorizeRequests.requestMatchers(url)
                                .hasAnyRole(roles.toArray(new String[0]));
                    });
                    authorizeRequests.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class) // Add JWT filter before UsernamePasswordAuthenticationFilter
                .addFilterAfter(jsonFilter, JwtAuthenticationFilter.class)                              // Add JSON filter after JWT filter
                .addFilterAfter(multipartFilter, JsonFilter.class);                                     // Add Multipart filter after JSON filter

        return http.build();
    }


    // Security context repository, purpose for saving security context
    // SpringContextPersistenceFilter deprecated in Spring 6.3.x
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}