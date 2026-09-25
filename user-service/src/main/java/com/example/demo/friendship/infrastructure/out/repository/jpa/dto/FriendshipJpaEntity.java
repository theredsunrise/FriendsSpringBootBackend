package com.example.demo.friendship.infrastructure.out.repository.jpa.dto;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.Instant;

@Entity
@Table(name = "friendships")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FriendshipJpaEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "id_friend", nullable = false)
    private Long friendId;

    @Column(name = "created_at", nullable = false)
    private Instant createdAt;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || Hibernate.getClass(this) != Hibernate.getClass(o)) {
            return false;
        }
        FriendshipJpaEntity that = (FriendshipJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
