package com.devcaiqueoliveira.nexus_api.dto;

import com.devcaiqueoliveira.nexus_api.entity.User;

import java.util.UUID;

public record UserResponse(
        UUID id,
        String name,
        String email
) {
    public UserResponse(User user) {
        this(user.getId(), user.getName(), user.getEmail());
    }
}
