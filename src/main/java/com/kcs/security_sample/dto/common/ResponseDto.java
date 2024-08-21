package com.kcs.security_sample.dto.common;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.kcs.security_sample.exception.CommonException;
import jakarta.annotation.Nullable;
import org.antlr.v4.runtime.misc.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.MethodArgumentNotValidException;

public record ResponseDto<T> (
        @JsonIgnore
        HttpStatus httpStatus,
        @NotNull
        boolean success,
        @Nullable
        T data,
        @Nullable
        ExceptionDto error
) {
    public static <T> ResponseDto<T> ok(T data) {
        return new ResponseDto<>(HttpStatus.OK, true, data, null);
    }

    public static <T> ResponseDto<T> created(T data) {
        return new ResponseDto<>(HttpStatus.CREATED, true, data, null);
    }

    public static ResponseDto<?> fail(CommonException e) {
        return new ResponseDto<>(e.getErrorCode().getHttpStatus(), false, null, new ExceptionDto(e.getErrorCode()));
    }

    public static ResponseDto<?> fail(MethodArgumentNotValidException e) {
        return new ResponseDto<>(HttpStatus.BAD_REQUEST, false, null, new ArgumentNotValidExceptionDto(e));
    }
}
