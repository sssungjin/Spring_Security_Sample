package com.kcs.security_sample.security.details;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.Collections;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String accountId;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

    public CustomUserDetails(String accountId, String password, String role) {
        this.accountId = accountId;
        this.password = password;
        this.authorities = Collections.singletonList(new SimpleGrantedAuthority("ROLE_" + role));
    }

    @Override
    public String getUsername() {
        return accountId;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true; // Adjust as needed
    }

    @Override
    public boolean isAccountNonLocked() {
        return true; // Adjust as needed
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true; // Adjust as needed
    }

    @Override
    public boolean isEnabled() {
        return true; // Adjust as needed
    }
}
