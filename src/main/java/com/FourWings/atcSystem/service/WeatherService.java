package com.FourWings.atcSystem.service;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;

public interface WeatherService {
    AirportWeatherInfo getCurrentWeatherForAirport(Airports airport);
}