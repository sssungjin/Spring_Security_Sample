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
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

//@Component
//@RequiredArgsConstructor
//@Order(Ordered.HIGHEST_PRECEDENCE)
//@Slf4j
//public class JwtAuthenticationFilter extends OncePerRequestFilter {
//    private final CustomUserDetailService customUserDetailService;
//    private final JwtService jwtService;
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//            throws ServletException, IOException {
//        log.info("JwtAuthenticationFilter processing request to '{}'", request.getRequestURI());
//
//        try {
//            String token = extractToken(request);
//            if (token != null && jwtService.validateToken(token)) {
//                Claims claims = jwtService.getClaimsFromToken(token);
//                String accountId = claims.getSubject();
//
//                // 매 요청마다 SecurityContext 생성 및 설정
//                SecurityContext context = SecurityContextHolder.createEmptyContext();
//                CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(accountId);
//                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
//                        userDetails, null, userDetails.getAuthorities());
//                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
//                context.setAuthentication(authentication);
//                SecurityContextHolder.setContext(context);
//
//                log.info("Authenticated user: {}, role: {}, setting security context", accountId, userDetails.getAuthorities());
//                log.info("SecurityContextHolder {}", SecurityContextHolder.getContext().getAuthentication());
//            } else {
//                log.info("No valid JWT token found, continuing without authentication");
//            }
//        } catch (Exception e) {
//            log.error("Unable to set user authentication: {}", e.getMessage());
//            SecurityContextHolder.clearContext();
//        }
//
//        filterChain.doFilter(request, response);
//        // 요청 처리 후 SecurityContext 정리
//        SecurityContextHolder.clearContext();
//    }
//
//    private String extractToken(HttpServletRequest request) {
//        String bearerToken = request.getHeader("Authorization");
//        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
//            return bearerToken.substring(7);
//        }
//        return null;
//    }
//}

@RequiredArgsConstructor
@Order(Ordered.HIGHEST_PRECEDENCE)
@Slf4j
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final CustomUserDetailService customUserDetailService;
    private final JwtService jwtService;
    private final SecurityContextRepository securityContextRepository;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        log.info("JwtAuthenticationFilter processing request to '{}'", request.getRequestURI());

        try {
            String token = extractToken(request);
            if (token != null && jwtService.validateToken(token)) {
                Claims claims = jwtService.getClaimsFromToken(token);
                String accountId = claims.getSubject();

                CustomUserDetails userDetails = (CustomUserDetails) customUserDetailService.loadUserByUsername(accountId);
                UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityService를 사용하여 SecurityContext 저장
                SecurityContext context = SecurityContextHolder.createEmptyContext();
                context.setAuthentication(authentication);
                SecurityContextHolder.setContext(context);
                securityContextRepository.saveContext(context, request, response);

                log.info("Authenticated user: {}, role: {}, setting security context", accountId, userDetails.getAuthorities());
                log.info("SecurityContextHolder {}", SecurityContextHolder.getContext().getAuthentication());
            } else {
                log.info("No valid JWT token found, continuing without authentication");
            }
        } catch (Exception e) {
            log.error("Unable to set user authentication: {}", e.getMessage());
            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(request, response);
    }

    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}