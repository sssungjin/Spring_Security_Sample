package com.kcs.security_sample.security.service;

import com.kcs.security_sample.domain.User;
import com.kcs.security_sample.dto.type.ERole;
import com.kcs.security_sample.dto.type.factory.ERoleFactory;
import com.kcs.security_sample.repository.UserRepository;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import io.jsonwebtoken.security.SignatureException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class JwtService {

    private final UserRepository userRepository;

    @Value("${jwt.secret-key}")
    private String secretKey;

    @Value("${jwt.expiration-time}")
    private long expirationTime;

    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateToken(User user) {
        log.info("Generating token for user: {}, role: {}", user.getAccountId(), user.getRole());

        List<String> authorities = user.getPermissions().stream()
                .map(permission -> permission.getUrl() + "_" + permission.getPermission().name())
                .collect(Collectors.toList());

        return Jwts.builder()
                .setSubject(user.getAccountId())
                .claim("role", user.getRole().name())
                .claim("name", user.getName())
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(getSigningKey()).build().parseClaimsJws(token);
            return true;
        } catch (SignatureException e) {
            log.error("Invalid JWT signature: {}", e.getMessage());
        } catch (MalformedJwtException e) {
            log.error("Invalid JWT token: {}", e.getMessage());
        } catch (ExpiredJwtException e) {
            log.error("JWT token is expired: {}", e.getMessage());
        } catch (UnsupportedJwtException e) {
            log.error("JWT token is unsupported: {}", e.getMessage());
        } catch (IllegalArgumentException e) {
            log.error("JWT claims string is empty: {}", e.getMessage());
        }
        return false;
    }

    public Claims getClaimsFromToken(String token) {
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getSigningKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody();
        } catch (ExpiredJwtException e) {
            log.warn("The token is expired and not valid anymore", e);
            return null;
        }
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromRole(String role) {
        ERole eRole = ERoleFactory.of(role);

        List<SimpleGrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_" + eRole.name()));

        Optional<User> userOptional = userRepository.findByRole(eRole);
        if (userOptional.isPresent()) {
            User user = userOptional.get();
        } else {
            log.warn("No user found with role: {}", role);
        }

        return authorities;
    }

    public List<SimpleGrantedAuthority> getAuthoritiesFromToken(String token) {
        Claims claims = getClaimsFromToken(token);
        List<String> authorities = claims.get("authorities", List.class);

        List<SimpleGrantedAuthority> grantedAuthorities = new ArrayList<>();
        grantedAuthorities.add(new SimpleGrantedAuthority("ROLE_" + claims.get("role", String.class)));

        for (String authority : authorities) {
            grantedAuthorities.add(new SimpleGrantedAuthority(authority.replace("/", "")));
        }

        return grantedAuthorities;
    }
}