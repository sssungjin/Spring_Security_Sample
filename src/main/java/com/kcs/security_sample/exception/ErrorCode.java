package com.kcs.security_sample.exception;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@RequiredArgsConstructor
@Getter
public enum ErrorCode {
    BAD_REQUEST(40000, HttpStatus.BAD_REQUEST, "잘못된 요청입니다."),
    INVALID_ROLE(40001, HttpStatus.BAD_REQUEST, "유효하지 않은 권한입니다."),
    INVALID_ARGUMENT(40002, HttpStatus.BAD_REQUEST, "요청에 유효하지 않은 인자입니다."),
    FAILURE_LOGIN(4003, HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),

    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),

    ;

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}