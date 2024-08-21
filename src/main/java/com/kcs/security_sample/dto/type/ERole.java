package com.kcs.security_sample.dto.type;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum ERole {
    USER("USER"),
    ADMIN("ADMIN"),
    MANAGER("MANAGER");

    private final String name;

    @Override
    public String toString() {
        return this.name;
    }
}

