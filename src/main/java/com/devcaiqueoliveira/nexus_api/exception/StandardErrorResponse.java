package com.devcaiqueoliveira.nexus_api.exception;

import java.time.LocalDateTime;

public record StandardErrorResponse(
        LocalDateTime timeStamp,
        Integer status,
        String error,
        String message,
        String path
) {
}
