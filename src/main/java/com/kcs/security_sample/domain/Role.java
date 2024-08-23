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
@Table(name = "role")
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(name = "name", nullable = false, unique = true)
    private ERole name;

    @OneToMany(mappedBy = "role")
    private Set<User> users = new HashSet<>();

    @OneToMany(mappedBy = "role")
    private Set<RoleUrlPermission> roleUrlPermissions = new HashSet<>();

    public Set<UrlPermission> getPermissions() {
        return roleUrlPermissions.stream()
                .map(RoleUrlPermission::getUrlPermission)
                .collect(Collectors.toSet());
    }
}
