package com.FourWings.atcSystem.model.airport;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Entity
@Table(name = "airports")
public class Airports {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "airport_id")
    private Long airportId;

    @Column(name = "icao_code", length = 4, nullable = false)
    private String icaoCode;

    @Column(name = "iata_code", length = 3)
    private String iataCode;

    @Column(name = "name")
    private String name;

    @Column(name = "city")
    private String city;

    @Column(name = "country")
    private String country;

    @Column(name = "timezone")
    private String timezone;

    @Column(name = "latitude", precision = 9, scale = 6)
    private BigDecimal latitude;

    @Column(name = "longitude", precision = 9, scale = 6)
    private BigDecimal longitude;

    @Column(name = "elevation")
    private Integer elevation;

    @Column(name = "address")
    private String address;

    @Column(name = "postal_code")
    private String postalCode;

    @Column(name = "website_url")
    private String websiteUrl;

    @Column(name = "phone_main")
    private String phoneMain;

    @Column(name = "email_main")
    private String emailMain;

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
        return "Airports{" +
                "airportId=" + airportId +
                ", icaoCode='" + icaoCode + '\'' +
                ", iataCode='" + iataCode + '\'' +
                ", name='" + name + '\'' +
                ", city='" + city + '\'' +
                ", country='" + country + '\'' +
                ", timezone='" + timezone + '\'' +
                ", latitude=" + latitude +
                ", longitude=" + longitude +
                ", elevation=" + elevation +
                ", address='" + address + '\'' +
                ", postalCode='" + postalCode + '\'' +
                ", websiteUrl='" + websiteUrl + '\'' +
                ", phoneMain='" + phoneMain + '\'' +
                ", emailMain='" + emailMain + '\'' +
                ", note='" + note + '\'' +
                ", createdAt=" + createdAt +
                ", updatedAt=" + updatedAt +
                '}';
    }
}