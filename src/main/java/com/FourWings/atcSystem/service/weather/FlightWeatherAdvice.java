// src/main/java/com/FourWings/atcSystem/service/weather/FlightWeatherAdvice.java
package com.FourWings.atcSystem.service.weather;

public record FlightWeatherAdvice(
        String status,      // "GO", "CAUTION", "DELAY"
        String explanation  // szöveges magyarázat
) {}