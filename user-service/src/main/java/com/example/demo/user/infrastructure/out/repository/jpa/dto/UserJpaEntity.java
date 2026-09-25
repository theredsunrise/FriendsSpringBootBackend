package com.example.demo.user.infrastructure.out.repository.jpa.dto;

import com.example.demo.user.infrastructure.out.repository.jpa.dto.projection.UserProjection;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.Hibernate;

import java.time.Instant;
import java.time.LocalDate;


@Entity
@Table(name = "users")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserJpaEntity implements UserProjection {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(nullable = false, length = 100)
    private String surname;


    @Column(nullable = false, length = 50, unique = true)
    private String username;


    @Column(name = "birth_date", nullable = false)
    private LocalDate birthDate;


    @Column(nullable = false, length = 150)
    private String residence;

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
        UserJpaEntity that = (UserJpaEntity) o;
        return id != null && id.equals(that.id);
    }

    @Override
    public int hashCode() {
        return Hibernate.getClass(this).hashCode();
    }
}
