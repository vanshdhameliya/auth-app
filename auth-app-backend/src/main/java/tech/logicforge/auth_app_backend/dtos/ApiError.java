package tech.logicforge.auth_app_backend.dtos;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp
) {

    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, LocalDateTime.now());
    }

    public static ApiError of(int status, String error, String message, String path,boolean notDateTime) {
        return new ApiError(status, error, message, path, null);
    }
}
