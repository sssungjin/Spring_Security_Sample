package com.kcs.security_sample.security.service;

import com.kcs.security_sample.domain.UrlPermission;
import com.kcs.security_sample.domain.User;
import com.kcs.security_sample.repository.UserRepository;
import com.kcs.security_sample.security.details.CustomUserDetails;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Set;

@Service
@RequiredArgsConstructor
@Slf4j
public class CustomUserDetailService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String accountId) throws UsernameNotFoundException {
        User user = userRepository.findByAccountId(accountId)
                .orElseThrow(() -> new UsernameNotFoundException("User not found: " + accountId));

        return new CustomUserDetails(user.getAccountId(), user.getPassword(), getAuthorities(user));
    }

    private Set<GrantedAuthority> getAuthorities(User user) {
        Set<GrantedAuthority> authorities = new HashSet<>();

        // Add role-based authority
        authorities.add(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()));

        // Add URL permissions
        for (UrlPermission permission : user.getPermissions()) {
            String authority = permission.getUrl() + "_" + permission.getPermission().name();
            authorities.add(new SimpleGrantedAuthority(authority));
        }

        return authorities;
    }

    public boolean hasPermission(String requiredPermission) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.info("Checking permission {} for user {}", requiredPermission, authentication);
        if (authentication == null) {
            return false;
        }

        return authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .anyMatch(auth -> auth.equals(requiredPermission));
    }
}
