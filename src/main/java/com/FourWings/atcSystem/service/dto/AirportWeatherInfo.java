package com.FourWings.atcSystem.service.dto;

public record AirportWeatherInfo(
        String emoji,
        double temperatureC,
        String conditionText,
        String windText,
        String visibilityText,
        String pressureText,
        String feelsLikeText,
        String updatedAtText,
        String metarRaw
) {}