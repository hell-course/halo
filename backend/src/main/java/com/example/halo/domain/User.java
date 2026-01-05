package com.example.halo.domain;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter; // Setter for password (hashed password)

@Entity
@Getter
@Setter // For password hashing
@NoArgsConstructor
@Table(name = "users") // Avoids conflict with 'user' keyword in some DBs
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(unique = true, nullable = false)
    private String username;

    @Column(nullable = false)
    private String password; // Will store hashed password

    @Column(unique = true, nullable = false)
    private String email;

    // Constructor for creating new users (without ID)
    public User(String username, String password, String email) {
        this.username = username;
        this.password = password;
        this.email = email;
    }
}
