package com.talkhub.backend.web;

import java.time.OffsetDateTime;
import java.util.List;

public record ApiErrorResponse(
    String code,
    String message,
    List<String> details,
    OffsetDateTime timestamp
) {

    public static ApiErrorResponse of(String code, String message, List<String> details) {
        return new ApiErrorResponse(code, message, details, OffsetDateTime.now());
    }
}
