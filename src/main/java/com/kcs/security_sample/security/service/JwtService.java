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

    // Get the signing key by using the secret key
    private SecretKey getSigningKey() {
        byte[] keyBytes = secretKey.getBytes(StandardCharsets.UTF_8);
        return Keys.hmacShaKeyFor(keyBytes);
    }

    // Parse the token and get the user's accountId
    public String generateToken(User user) {
        List<String> authorities = user.getPermissions().stream()
                .map(permission -> permission.getUrl() + "_" + permission.getPermission().name())
                .collect(Collectors.toList());

        // Generate the token which contains the user's accountId, role, name, authorities, issuedAt, expiration, and signing key
        return Jwts.builder()
                .setSubject(user.getAccountId())
                .claim("role", user.getRole().getName())
                .claim("name", user.getName())
                .claim("authorities", authorities)
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    // Parse the token and get the user's accountId
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
}