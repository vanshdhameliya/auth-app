package tech.logicforge.auth_app_backend.paylod;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import tech.logicforge.auth_app_backend.entity.Provider;
import lombok.*;

import java.time.Instant;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UserDto {

    private UUID id;

    // @NotBlank ensures it's not null, not empty, and doesn't contain only whitespace
    @NotBlank(message = "Email is required")
    @Email(message = "Please provide a valid email address")
    private String email;

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @NotBlank(message = "Password is required")
    @Size(min = 6, message = "Password must be at least 6 characters long")
    private String password;

    private String image;

    private boolean enable = true;

    private Instant createdAt = Instant.now();
    private Instant updatedAt = Instant.now();

    @Builder.Default
    private Provider provider = Provider.LOCAL;

    private Set<RoleDto> roles = new HashSet<>();
}
