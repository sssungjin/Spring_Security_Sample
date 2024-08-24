package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1")
public class LoginController {
    private final UserService userService;

    @PostMapping("/login")
    public ResponseDto<?> login(@RequestAttribute("account_id") String accountId,
                                @RequestAttribute("password") String password) {
        log.info("Login attempt for account: {}", accountId);
        try {
            return userService.login(accountId, password);
        } catch (Exception e) {
            log.error("Login error for account: {}", accountId, e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }
}
