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
    FAILURE_LOGIN(40003, HttpStatus.BAD_REQUEST, "로그인에 실패하였습니다."),
    FILE_UPLOAD_FAILED(40004, HttpStatus.BAD_REQUEST, "파일 업로드에 실패하였습니다."),
    FILE_EXTENSION_NOT_ALLOWED(40005, HttpStatus.BAD_REQUEST, "허용되지 않은 파일 확장자입니다."),
    FILE_PERMISSION_SETTING_FAILED(40006, HttpStatus.BAD_REQUEST, "파일 권한 설정에 실패하였습니다."),
    INVALID_PERMISSION(40007, HttpStatus.BAD_REQUEST, "유효하지 않은 권한입니다."),
    INVALID_INPUT(40008, HttpStatus.BAD_REQUEST, "유효하지 않은 입력입니다."),
    INVALID_PATH_TYPE(40009, HttpStatus.BAD_REQUEST, "유효하지 않은 Path Type 입니다."),
    EMPTY_FILE(40010, HttpStatus.BAD_REQUEST, "업로드 할 파일이 존재하지 않습니다."),


    UNAUTHORIZED(40100, HttpStatus.UNAUTHORIZED, "인증되지 않은 사용자입니다."),


    INTERNAL_SERVER_ERROR(50000, HttpStatus.INTERNAL_SERVER_ERROR, "서버 내부 에러입니다."),

    ;

    private final int code;
    private final HttpStatus httpStatus;
    private final String message;
}