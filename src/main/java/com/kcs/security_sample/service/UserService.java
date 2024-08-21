package com.kcs.security_sample.service;

import com.kcs.security_sample.domain.User;
import com.kcs.security_sample.dto.common.ResponseDto;
import com.kcs.security_sample.dto.response.LoginResponseDto;
import com.kcs.security_sample.exception.CommonException;
import com.kcs.security_sample.exception.ErrorCode;
import com.kcs.security_sample.repository.UserRepository;
import com.kcs.security_sample.security.service.JwtService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class UserService {
    private final UserRepository userRepository;
    private final JwtService jwtService;

    public Optional<User> findByAccountId(String accountId) {
        Optional<User> user = userRepository.findByAccountId(accountId);
        log.info("Found user for accountId: {}, ROLE: {}", accountId, user.isPresent() ? user.get().getRole() : "not found");
        return user;
    }

    public boolean authenticate(String accountId, String password) {
        Optional<User> user = findByAccountId(accountId);
        boolean isAuthenticated = user.isPresent() && password.equals(user.get().getPassword());
        log.info("Authentication attempt for accountId {}, Authenticate: {}", accountId, isAuthenticated ? "success" : "failure");
        return isAuthenticated;
    }

    public ResponseDto<?> login(String accountId, String password) {
        try {
            User user = findByAccountId(accountId)
                    .orElseThrow(() -> new CommonException(ErrorCode.FAILURE_LOGIN));
            if (!authenticate(accountId, password)) {
                throw new CommonException(ErrorCode.FAILURE_LOGIN);
            }
            String token = jwtService.generateToken(user);
            log.info("Login successful for account: {}", accountId);
            return ResponseDto.ok(new LoginResponseDto(token));
        } catch (CommonException e) {
            log.warn("Login failed for account: {}", accountId);
            return ResponseDto.fail(e);
        } catch (Exception e) {
            log.error("Unexpected error during login for account: {}", accountId, e);
            return ResponseDto.fail(new CommonException(ErrorCode.INTERNAL_SERVER_ERROR));
        }
    }

}
