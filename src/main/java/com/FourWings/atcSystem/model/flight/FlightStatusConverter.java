package com.FourWings.atcSystem.model.flight;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class FlightStatusConverter implements AttributeConverter<FlightStatus, String> {

    @Override
    public String convertToDatabaseColumn(FlightStatus attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }

    @Override
    public FlightStatus convertToEntityAttribute(String dbData) {
        return dbData == null ? null : FlightStatus.valueOf(dbData.toUpperCase());
    }
}
