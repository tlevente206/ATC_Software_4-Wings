package com.FourWings.atcSystem.model.airline;


import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class BusinessModeConverter implements AttributeConverter<BusinessMode, String> {
    @Override
    public String convertToDatabaseColumn(BusinessMode attribute) {
        return attribute == null ? null : attribute.name().toLowerCase();
    }
    @Override
    public BusinessMode convertToEntityAttribute(String dbData) {
        return dbData == null ? null : BusinessMode.valueOf(dbData.toUpperCase());
    }
}
