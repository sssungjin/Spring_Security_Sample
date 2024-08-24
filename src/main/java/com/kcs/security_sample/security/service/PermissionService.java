package com.kcs.security_sample.security.service;

import com.kcs.security_sample.domain.Role;
import com.kcs.security_sample.domain.RoleUrlPermission;
import com.kcs.security_sample.domain.UrlPermission;
import com.kcs.security_sample.dto.response.PermissionDto;
import com.kcs.security_sample.repository.RoleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class PermissionService {
    private final RoleRepository roleRepository;

    // Get all permissions from DB
    @Transactional(readOnly = true)
    public List<PermissionDto> getAllPermissions() {
        List<Role> roles = roleRepository.findAll();
        List<PermissionDto> permissions = new ArrayList<>();

        // Get all permissions from each role
        for (Role role : roles) {
            for (RoleUrlPermission roleUrlPermission : role.getRoleUrlPermissions()) {
                UrlPermission urlPermission = roleUrlPermission.getUrlPermission();
                permissions.add(new PermissionDto(
                        role.getName(),
                        urlPermission.getUrl(),
                        urlPermission.getPermission()
                ));
            }
        }
        return permissions;
    }
}

