package com.stock.stock.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "app_users")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AppUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String password;

    // e.g., "ROLE_EMPLOYEE" or "ROLE_ADMIN"
    private String roles;

    @Column(nullable = false)
    private Boolean enabled = true;

    @ManyToOne
    @JoinColumn(name = "store_stand_id", unique = true)
    private StoreStand storeStand;

    @Column(columnDefinition = "TEXT")
    private String refreshToken;
}