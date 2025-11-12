package com.FourWings.atcSystem.model.gate;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class GateStatusConverter implements AttributeConverter<GateStatus, String> {
    @Override public String convertToDatabaseColumn(GateStatus s) {
        return s == null ? null : s.name().toLowerCase();
    }
    @Override public GateStatus convertToEntityAttribute(String v) {
        return v == null ? null : GateStatus.valueOf(v.toUpperCase());
    }
}
