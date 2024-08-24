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
        List<PermissionDto> permissions = permissionService.getAllPermissions();
        log.info("Permissions: {}", permissions);

        log.info("SecurityContextHolder {}", SecurityContextHolder.getContext().getAuthentication());

        permissions.forEach(permission ->
                log.info("Permission: {}, url: {}\n, role: {}, role.name: {}", permission, permission.url(), permission.role(), permission.role().name())
        );

        Map<String, Set<String>> urlRoles = new HashMap<>();
        permissions.forEach(permission -> {
            urlRoles.computeIfAbsent(permission.url(), k -> new HashSet<>())
                    .add(permission.role().name());
        });

        http
                .csrf(AbstractHttpConfigurer::disable)
                .authorizeHttpRequests(authorizeRequests -> {
                    authorizeRequests
                            .requestMatchers(
                                    "/login", "/api/v1/login", "/api/v1/submit/danger",
                                    "/api/v1/upload/file", "/api/v1/submit/formdata",
                                    "/api/v1/jsonfile/upload", "/api/v1/multipart/upload")
                            .permitAll()
                            .requestMatchers("/user").hasAnyRole("ADMIN", "USER", "MANAGER")
                            .requestMatchers("/settlement").hasAnyRole("MANAGER", "ADMIN")
                            .requestMatchers("/admin").hasRole("ADMIN");

                    urlRoles.forEach((url, roles) -> {
                        authorizeRequests.requestMatchers(url)
                                .hasAnyRole(roles.toArray(new String[0]));
                    });
                    authorizeRequests.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jsonFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(multipartFilter, JsonFilter.class);

        return http.build();
    }


    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new DelegatingSecurityContextRepository(
                new RequestAttributeSecurityContextRepository(),
                new HttpSessionSecurityContextRepository()
        );
    }
}