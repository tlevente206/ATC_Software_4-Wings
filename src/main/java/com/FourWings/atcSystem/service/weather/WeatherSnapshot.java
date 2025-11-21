// src/main/java/com/FourWings/atcSystem/service/weather/WeatherSnapshot.java
package com.FourWings.atcSystem.service.weather;

public record WeatherSnapshot(
        double windSpeed,
        double windGust,
        int windDeg,
        double visibilityKm,
        int cloudCoverage,
        boolean thunderstorm,
        boolean heavyRainOrSnow,
        double temperature,
        double pressure,
        double humidity
) {}