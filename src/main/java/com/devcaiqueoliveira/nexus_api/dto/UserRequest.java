package com.devcaiqueoliveira.nexus_api.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record UserRequest (

        @NotBlank(message = "{user.name.required}")
        String name,

        @NotBlank(message = "{user.email.required}")
        @Email(message = "{user.email.invalid}")
        String email,

        @NotBlank(message = "{user.password.required}")
        @Size(min = 8, message = "{user.password.size}")
        String password
){
}
