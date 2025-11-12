package com.FourWings.atcSystem.model.terminal;

import com.FourWings.atcSystem.model.airport.Airports;
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
@Table(name = "terminals")
@ToString(exclude = "airport")   
public class Terminal {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "terminal_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "airport_id", referencedColumnName = "airport_id")
    private Airports airport;

    @Column(name = "code", length = 20, nullable = false)
    private String code;

    @Convert(converter = TerminalStatusConverter.class) // DB-ben kisbetűs
    @Column(name = "status")
    private TerminalStatus status;

    @Column(name = "gates_count")
    private Short gatesCount;

    @Column(name = "has_departures_hall")
    private Boolean hasDeparturesHall;

    @Column(name = "has_arrivals_hall")
    private Boolean hasArrivalsHall;

    @Column(name = "has_cargo_facility")
    private Boolean hasCargoFacility;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}