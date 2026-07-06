package com.devcaiqueoliveira.nexus_api.dto;

public record UserRequest (
        String name,
        String email,
        String password
){
}
