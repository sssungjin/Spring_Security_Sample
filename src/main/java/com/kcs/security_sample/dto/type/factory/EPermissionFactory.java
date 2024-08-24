package com.kcs.security_sample.dto.type.factory;

import com.kcs.security_sample.dto.type.EPermission;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;

public class EPermissionFactory {
    public static EPermission of(String permission) {
        return switch (permission.toUpperCase()) {
            case "CREATE" -> EPermission.CREATE;
            case "READ" -> EPermission.READ;
            case "UPDATE" -> EPermission.UPDATE;
            case "DELETE" -> EPermission.DELETE;
            default -> throw new CommonException(ErrorCode.INVALID_PERMISSION);
        };
    }
}