package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.request.LoginRequestDto;
import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class LoginController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseDto<?> login(@RequestBody LoginRequestDto loginRequest) {
        log.info("Login attempt for account: {}", loginRequest.accountId());
        try {
            return userService.login(loginRequest.accountId(), loginRequest.password());
        } catch (Exception e) {
            log.error("Login error for account: {}", loginRequest.accountId(), e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}
