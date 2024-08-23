package com.kcs.security_sample.dto.response;

import com.kcs.security_sample.dto.type.EPermission;
import com.kcs.security_sample.dto.type.ERole;

public record PermissionDto(ERole role, String url, EPermission permission) {
    public PermissionDto(ERole role, String url, EPermission permission) {
        this.role = role;
        this.url = url;
        this.permission = permission;
    }
}
