package com.kcs.security_sample.security.config;

import com.kcs.security_sample.dto.response.PermissionDto;
import com.kcs.security_sample.security.filter.JsonFilter;
import com.kcs.security_sample.security.filter.JwtAuthenticationFilter;
import com.kcs.security_sample.security.filter.MultipartFilter;
import com.kcs.security_sample.security.service.PermissionService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.security.Permission;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final MultipartFilter multipartFilter;
    private final JsonFilter jsonFilter;
    private final PermissionService permissionService;

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

        http
                .cors(Customizer.withDefaults())
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

                    permissions.forEach(permission ->
                            authorizeRequests.requestMatchers(permission.url())
                                    .hasRole(permission.role().name())
                    );

                    authorizeRequests.anyRequest().authenticated();
                })
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterAfter(jsonFilter, JwtAuthenticationFilter.class)
                .addFilterAfter(multipartFilter, JsonFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}