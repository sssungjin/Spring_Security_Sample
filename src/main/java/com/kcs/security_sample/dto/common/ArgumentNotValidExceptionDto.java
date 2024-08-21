package com.kcs.security_sample.dto.common;

import com.kcs.security_sample.exception.ErrorCode;
import lombok.Getter;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.HashMap;
import java.util.Map;

@Getter
public class ArgumentNotValidExceptionDto extends ExceptionDto {
    private final MethodArgumentNotValidException methodArgumentNotValidException;
    @Getter
    private final Map<String, String> errorFields;

    public ArgumentNotValidExceptionDto(MethodArgumentNotValidException methodArgumentNotValidException) {
        super(ErrorCode.INVALID_ARGUMENT);
        this.methodArgumentNotValidException = methodArgumentNotValidException;
        this.errorFields = new HashMap<>();

        for (ObjectError objectError : methodArgumentNotValidException.getBindingResult().getAllErrors()) {
            FieldError fieldError = (FieldError) objectError;
            errorFields.put(fieldError.getField(), fieldError.getDefaultMessage());
        }
    }
}

