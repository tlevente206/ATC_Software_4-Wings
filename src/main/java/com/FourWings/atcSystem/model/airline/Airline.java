package com.FourWings.atcSystem.model.airline;

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
@Table(name = "airlines")

public class Airline {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "airline_id")
    private Long id;

    @Column(name = "name", length = 100, nullable = false)
    private String name;

    @Column(name = "icao_code", length = 3)
    private String icaoCode;

    @Column(name = "iata_code", length = 2)
    private String iataCode;

    @Column(name = "country", length = 100)
    private String country;

    @Column(name = "founded_year")
    private Short foundedYear;

    @Column(name = "is_active")
    private Boolean active;

    @Convert(converter = BusinessModeConverter.class)   // DB-ben kisbetűs enum értékek
    @Column(name = "business_mode")
    private BusinessMode businessMode;

    // Ha szeretnéd tényleges FK-ként kezelni:
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_airport_id", referencedColumnName = "airport_id")
    private Airports baseAirport;

    // Ha inkább csak az ID kéne, a fenti helyett:
    // @Column(name = "base_airport_id")
    // private Long baseAirportId;

    @Column(name = "website_url", length = 255)
    private String websiteUrl;

    @Column(name = "phone_main", length = 50)
    private String phoneMain;

    @Column(name = "email_main", length = 100)
    private String emailMain;

    @Column(name = "headquarters_address", length = 255)
    private String headquartersAddress;

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
        return "Airline{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", icaoCode='" + icaoCode + '\'' +
                ", iataCode='" + iataCode + '\'' +
                ", country='" + country + '\'' +
                ", foundedYear=" + foundedYear +
                ", active=" + active +
                ", businessMode=" + businessMode +
                ", baseAirport=" + baseAirport +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", phoneMain='" + phoneMain + '\'' +
                ", emailMain='" + emailMain + '\'' +
                ", headquartersAddress='" + headquartersAddress + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}