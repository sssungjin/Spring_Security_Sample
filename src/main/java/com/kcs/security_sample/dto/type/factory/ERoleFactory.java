package com.kcs.security_sample.dto.type.factory;

import com.kcs.security_sample.dto.type.ERole;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;

public class ERoleFactory {
    public static ERole of(String role) {
        return switch (role) {
            case "USER" -> ERole.USER;
            case "ADMIN" -> ERole.ADMIN;
            case "GUEST" -> ERole.MANAGER;
            default -> throw new CommonException(ErrorCode.INVALID_ROLE);
        };
    }
}

