package com.devcaiqueoliveira.nexus_api.exception;

import org.springframework.validation.FieldError;

import java.util.List;

public record ValidationErrorResponse(
        int status,
        String error,
        List<FieldErrorDetail> fields
) {
    public record FieldErrorDetail(String field, String message ){}
}
