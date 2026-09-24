package tech.logicforge.auth_app_backend.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record ApiError(
        int status,
        String error,
        String message,
        String path,
        LocalDateTime timestamp,
        Map<String, String> fieldErrors
) {

    // Standard constructor fallback for global errors (sets fieldErrors to null or empty map)
    public static ApiError of(int status, String error, String message, String path) {
        return new ApiError(status, error, message, path, LocalDateTime.now(), null);
    }

    // Factory method specifically handling validation failures with field errors
    public static ApiError of(int status, String error, String message, String path, Map<String, String> fieldErrors) {
        return new ApiError(status, error, message, path, LocalDateTime.now(), fieldErrors);
    }
}
