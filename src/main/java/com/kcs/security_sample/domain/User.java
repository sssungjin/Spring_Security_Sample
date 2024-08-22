package com.kcs.security_sample.domain;

import com.kcs.security_sample.dto.type.ERole;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "user")
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "account_id", nullable = false, unique = true)
    private String accountId;

    @Column(name = "password", nullable = false)
    private String password;

    @Column(name = "name", nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    private ERole role;

    @OneToMany(mappedBy = "user", fetch = FetchType.EAGER)
    private Set<UserUrlPermission> userUrlPermissions = new HashSet<>();

    public Set<UrlPermission> getPermissions() {
        return userUrlPermissions.stream()
                .map(UserUrlPermission::getUrlPermission)
                .collect(Collectors.toSet());
    }
}
