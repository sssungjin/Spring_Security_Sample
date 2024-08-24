package com.kcs.security_sample.controller;

import com.kcs.security_sample.dto.common.ResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class PageController {

    @GetMapping("/admin")
    public ResponseDto<?> adminPage() {
        return ResponseDto.ok("Admin page");
    }

    @GetMapping("/settlement")
    public ResponseDto<?> settlementPage() {
        return ResponseDto.ok("Settlement page");
    }

    @GetMapping("/user")
    public ResponseDto<?> userPage() {
        return ResponseDto.ok("User page");
    }
}
