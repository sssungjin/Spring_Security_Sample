package com.kcs.security_sample.service;

import com.kcs.security_sample.dto.request.SubmitRequestDto;
import com.kcs.security_sample.dto.response.SubmitResponseDto;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;


@Service
@RequiredArgsConstructor
public class SubmitService {
    public SubmitResponseDto processDangerSubmit(SubmitRequestDto submitRequest) {
        return new SubmitResponseDto(submitRequest.inputText());
    }


}