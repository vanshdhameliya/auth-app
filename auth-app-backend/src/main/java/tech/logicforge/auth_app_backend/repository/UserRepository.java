package tech.logicforge.auth_app_backend.repository;

import tech.logicforge.auth_app_backend.entity.Provider;
import tech.logicforge.auth_app_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface UserRepository extends JpaRepository<User, UUID> {

    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    Optional<User> findByProviderAndProviderId(
            Provider provider,
            String providerId
    );
}

