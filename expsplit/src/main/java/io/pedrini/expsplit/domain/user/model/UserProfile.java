package io.pedrini.expsplit.domain.user.model;

import java.time.Instant;

public class UserProfile {

    private final UserProfileId id;
    private final UserProfileEmail email;
    private FirstName firstName;
    private LastName lastName;
    private final Instant createdAt;

    public UserProfile(UserProfileId id, UserProfileEmail email, FirstName firstName, LastName lastName, Instant createdAt) {
        this.id = id;
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.createdAt = createdAt;
    }

    public static UserProfile create(UserProfileId id, UserProfileEmail email) {
        return new UserProfile(id, email, null, null, Instant.now());
    }

    public void updateName(FirstName firstName, LastName lastName) {
        this.firstName = firstName;
        this.lastName = lastName;
    }

    public UserProfileId id() { return id; }
    public UserProfileEmail email() { return email; }
    public FirstName firstName() { return firstName; }
    public LastName lastName() { return lastName; }
    public Instant createdAt() { return createdAt; }
}
