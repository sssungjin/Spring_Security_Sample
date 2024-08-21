package com.kcs.security_sample.dto.common;


import com.kcs.security_sample.exception.ErrorCode;
import lombok.Getter;

@Getter
public class ExceptionDto {
    private final int code;
    private final String message;

    public ExceptionDto(ErrorCode errorCode) {
        this.code = errorCode.getCode();
        this.message = errorCode.getMessage();
    }
}