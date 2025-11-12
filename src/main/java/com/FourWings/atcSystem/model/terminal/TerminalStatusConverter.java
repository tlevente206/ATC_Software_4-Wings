package com.FourWings.atcSystem.model.terminal;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter(autoApply = true)
public class TerminalStatusConverter implements AttributeConverter<TerminalStatus, String> {
    @Override public String convertToDatabaseColumn(TerminalStatus s) {
        return s == null ? null : s.name().toLowerCase();
    }
    @Override public TerminalStatus convertToEntityAttribute(String db) {
        return db == null ? null : TerminalStatus.valueOf(db.toUpperCase());
    }
}
