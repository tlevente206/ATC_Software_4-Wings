package com.FourWings.atcSystem.model.aircraft;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class AircraftStatusConverter implements AttributeConverter<AircraftStatus, String> {
    @Override
    public String convertToDatabaseColumn(AircraftStatus status) {
        return status == null ? null : status.name().toLowerCase();
    }

    @Override
    public AircraftStatus convertToEntityAttribute(String dbValue) {
        return dbValue == null ? null : AircraftStatus.valueOf(dbValue.toUpperCase());
    }
}
