package com.kcs.security_sample.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record LoginRequestDto(
        @JsonProperty("account_id")
        String accountId,

        @JsonProperty("password")
        String password
) { }