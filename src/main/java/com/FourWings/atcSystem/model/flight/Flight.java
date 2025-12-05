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

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "flights")
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

    @Convert(converter = FlightStatusConverter.class)
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

    // --- UI-hoz használt, nem perzisztált mezők (transient) ---

    @Transient
    private String airlineName;             // légitársaság neve

    @Transient
    private String originName;              // induló reptér neve

    @Transient
    private String destinationName;         // érkező reptér neve

    @Transient
    private String scheduledDepartureText;  // menetrend szerinti indulás (formázott)

    @Transient
    private String estimatedDepartureText;  // várható indulás (formázott)

    @Transient
    private String scheduledArrivalText;    // menetrend szerinti érkezés (formázott)

    @Transient
    private String estimatedArrivalText;    // várható érkezés (formázott)

    @Transient
    private String gateCode;                // kapukód

    @Transient
    private String statusText;              // státusz szöveg (UI-hoz)

    // --- Aircraft (repülőgép) adatok a UI számára ---

    @Transient
    private String aircraftRegistration;

    @Transient
    private String aircraftTypeIcao;

    @Transient
    private Short aircraftMaxSeatCapacity;

    @Transient
    private Short aircraftManufactureYear;

    @Transient
    private String aircraftStatusText;

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Flight flight = (Flight) o;
        // ha az id még nincs elmentve (null), akkor objektum azonosság szerint hasonlítunk
        if (id == null || flight.id == null) return false;
        return id.equals(flight.id);
    }

    @Override
    public int hashCode() {
        return 31;
    }

    @Override
    public String toString() {
        return "Flight{" +
                "id=" + id +
                ", flightNumber='" + flightNumber + '\'' +
                ", status=" + status +
                ", scheduledDeparture=" + scheduledDeparture +
                ", scheduledArrival=" + scheduledArrival +
                ", estimatedDeparture=" + estimatedDeparture +
                ", estimatedArrival=" + estimatedArrival +
                '}';
    }
}