package com.kcs.security_sample.security.details;

import com.kcs.security_sample.domain.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;

@Getter
@RequiredArgsConstructor
public class CustomUserDetails implements UserDetails {
    private final String accountId;
    private final String password;
    private final Collection<? extends GrantedAuthority> authorities;

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
