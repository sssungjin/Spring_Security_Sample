package com.kcs.security_sample.security.filter;

import com.kcs.security_sample.security.details.CustomUserDetails;
import com.kcs.security_sample.security.service.CustomUserDetailService;
import com.kcs.security_sample.security.service.JwtService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

@Component
@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final CustomUserDetailService customUserDetailService;

    private final JwtService jwtService;

    UsernamePasswordAuthenticationToken authentication = null;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JwtAuthenticationFilter processing request to '{}'", request.getRequestURI());

        try {
            String token = extractToken(request);
            if (token != null && jwtService.validateToken(token)) {
                Claims claims = jwtService.getClaimsFromToken(token);

                String accountId = claims.getSubject();
                String role = claims.get("role", String.class);

                log.info("JWT claims - accountId: {}, role: {}", accountId, role);

                CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(accountId);

                authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));

                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                SecurityContextHolder.getContext().setAuthentication(authentication);
                log.info("authentication: {}", SecurityContextHolder.getContext().getAuthentication());
                log.info("Authenticated user: {}, role: {}, setting security context", accountId, role);
            }
        } catch (Exception e) {
            log.error("Unable to set user authentication: {}", e.getMessage());
        }

        log.info("Before filter chain - SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());
        filterChain.doFilter(request, response);
        SecurityContextHolder.getContext().setAuthentication(authentication);

        log.info("After filter chain - SecurityContext: {}", SecurityContextHolder.getContext().getAuthentication());
    }




    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}