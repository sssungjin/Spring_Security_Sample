package com.kcs.security_sample.dto.request;

import com.fasterxml.jackson.annotation.JsonProperty;

public record SubmitRequestDto(
        @JsonProperty("input_text")
       String inputText
) { }

