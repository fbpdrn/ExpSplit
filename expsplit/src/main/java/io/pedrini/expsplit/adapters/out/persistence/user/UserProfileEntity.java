package io.pedrini.expsplit.adapters.out.persistence.user;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "user_profile")
class UserProfileEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String email;

    private String firstName;

    private String lastName;

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected UserProfileEntity() { }

    UserProfileEntity(UUID id, String email, String firstName, String lastName, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getEmail() { return email; }
    String getFirstName() { return firstName; }
    String getLastName() { return lastName; }
    Instant getCreatedAt() { return createdAt; }
}
