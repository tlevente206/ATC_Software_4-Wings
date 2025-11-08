package com.FourWings.atcSystem.model.aircraft;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "aircraft")
public class Aircraft {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "aircraft_id")
    private Long id;

    @Column(name = "registration", length = 20, nullable = false)
    private String registration;

    @Column(name = "type_icao", length = 10)
    private String typeIcao;

    @Column(name = "airline_id")
    private Long airlineId;

    @Convert(converter = AircraftStatusConverter.class)
    @Column(name = "status")
    private AircraftStatus status;

    @Column(name = "max_seat_capacity")
    private Short maxSeatCapacity;

    @Column(name = "cargo_capacity_base")
    private Integer cargoCapacityBase;

    @Column(name = "base_airport_id")
    private Long baseAirportId;

    @Column(name = "manufacture_year")
    private Short manufactureYear;

    @Column(name = "note", columnDefinition = "TEXT")
    private String note;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Override
    public String toString() {
        return "Aircraft{" +
                "id=" + id +
                ", registration='" + registration + '\'' +
                ", typeIcao='" + typeIcao + '\'' +
                ", airlineId=" + airlineId +
                ", status=" + status +
                ", maxSeatCapacity=" + maxSeatCapacity +
                ", cargoCapacityBase=" + cargoCapacityBase +
                ", baseAirportId=" + baseAirportId +
                ", manufactureYear=" + manufactureYear +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}
