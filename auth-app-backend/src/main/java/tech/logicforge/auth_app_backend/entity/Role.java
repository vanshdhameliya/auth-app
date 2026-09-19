package tech.logicforge.auth_app_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.util.UUID;

@Getter
@Setter
@AllArgsConstructor
@Builder
@NoArgsConstructor
@Entity
@Table(name = "roles")
public class Role {

    @Id
    private UUID id = UUID.randomUUID();

    @Column(unique = true, nullable = false)
    private String name;
}
