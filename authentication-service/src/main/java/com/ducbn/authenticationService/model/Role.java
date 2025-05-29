package com.ducbn.authenticationService.model;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "roles")
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
@Builder
public class Role {
    public static String ADMIN = "ADMIN";
    public static String USER = "USER";
    public static String BUS_OWNER = "BUS_OWNER";
    public static String COMPANY = "COMPANY";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "permission")
    private String permissions;
}
