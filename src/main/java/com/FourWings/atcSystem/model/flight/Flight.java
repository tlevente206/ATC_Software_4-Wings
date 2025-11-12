package com.FourWings.atcSystem.model.flight;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.model.airline.Airline;
import com.FourWings.atcSystem.model.aircraft.Aircraft;
import com.FourWings.atcSystem.model.gate.Gate;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "flights")
@ToString(exclude = {"departureAirport", "arrivalAirport", "airline", "aircraft", "gate"})
public class Flight {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "flight_id")
    private Long id;

    // --- Kapcsolatok ---
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "departure_airport_id", referencedColumnName = "airport_id")
    private Airports departureAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "arrival_airport_id", referencedColumnName = "airport_id")
    private Airports arrivalAirport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airline_id", referencedColumnName = "airline_id")
    private Airline airline;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "aircraft_id", referencedColumnName = "aircraft_id")
    private Aircraft aircraft;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gate_id", referencedColumnName = "gate_id")
    private Gate gate;

    // --- Adatok ---
    @Column(name = "flight_number", length = 10, nullable = false)
    private String flightNumber;

    @Convert(converter = FlightStatusConverter.class) // csak ha kisbetűs enum az adatbázisban
    @Enumerated(EnumType.STRING)
    @Column(name = "status")
    private FlightStatus status;

    // --- Időpontok ---
    @Column(name = "scheduled_departure")
    private LocalDateTime scheduledDeparture;

    @Column(name = "scheduled_arrival")
    private LocalDateTime scheduledArrival;

    @Column(name = "estimated_departure")
    private LocalDateTime estimatedDeparture;

    @Column(name = "actual_departure")
    private LocalDateTime actualDeparture;

    @Column(name = "estimated_arrival")
    private LocalDateTime estimatedArrival;

    @Column(name = "actual_arrival")
    private LocalDateTime actualArrival;

    // --- Egyéb mezők ---
    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    // --- Létrehozás és módosítás ---
    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}