package tech.logicforge.auth_app_backend.paylod;

public record LoginRequest(
        String email,
        String password
) {
}
