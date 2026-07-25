package io.pedrini.expsplit.adapters.out.persistence.group;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Table;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "groups")
class GroupEntity {

    @Id
    private UUID id;

    @Column(nullable = false)
    private String name;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "group_membership", joinColumns = @JoinColumn(name = "group_id"))
    private List<MembershipEmbeddable> members = new ArrayList<>();

    @Column(nullable = false, updatable = false)
    private Instant createdAt;

    protected GroupEntity() { }

    GroupEntity(UUID id, String name, List<MembershipEmbeddable> members, Instant createdAt) {
        this.id = id;
        this.name = name;
        this.members = members;
        this.createdAt = createdAt;
    }

    UUID getId() { return id; }
    String getName() { return name; }
    List<MembershipEmbeddable> getMembers() { return members; }
    Instant getCreatedAt() { return createdAt; }
}
