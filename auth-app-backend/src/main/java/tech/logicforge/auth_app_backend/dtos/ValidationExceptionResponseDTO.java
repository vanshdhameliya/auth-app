package tech.logicforge.auth_app_backend.dtos;

import java.time.LocalDateTime;
import java.util.Map;

public record ValidationExceptionResponseDTO(
        LocalDateTime timeStamp,
        Integer statusCode,
        String error,
        String message,
        String path,
        Map<String,String> fieldErrors)
{
}
