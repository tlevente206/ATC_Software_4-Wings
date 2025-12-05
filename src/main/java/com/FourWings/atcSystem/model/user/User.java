package com.FourWings.atcSystem.model.user;

import com.FourWings.atcSystem.model.airport.Airports;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "user")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long id;

    private String name;

    @Column(unique = true, nullable = false)
    private String username;

    private String password;
    private String email;
    private String phone;

    /**
     * Szerepkör:
     *  - USER       = sima felhasználó
     *  - CONTROLLER = adott repülőtérhez rendelt irányító
     *  - ADMIN      = teljes jogú admin
     */
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private UserRole role;

    /**
     * Ha a user CONTROLLER, ide kötjük az "otthoni" repteret.
     * USER / ADMIN esetén lehet null.
     *
     * EAGER-re állítjuk, hogy JavaFX oldalon (comboBox, táblázat)
     * gond nélkül tudjuk használni, még akkor is, ha a Hibernate session már lezárult.
     */
    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "assigned_airport_id")
    private Airports assignedAirport;

    /**
     * Profilkép elérési útja (pl. "/images/avatars/avatar3.png"),
     * vagy null, ha nincs.
     */
    @Column(name = "profile_image_path")
    private String profileImagePath;

    // KÉNYELMI METÓDUSOK

    public boolean isAdmin() {
        return role == UserRole.ADMIN;
    }

    public boolean isController() {
        return role == UserRole.CONTROLLER;
    }

    public boolean isUserOnly() {
        return role == UserRole.USER;
    }
}