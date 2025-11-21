// src/main/java/com/FourWings/atcSystem/service/weather/FlightWeatherAdvisorService.java
package com.FourWings.atcSystem.service.weather;

import com.FourWings.atcSystem.model.airport.Airports;
import org.springframework.stereotype.Service;

@Service
public class FlightWeatherAdvisorService {

    private final WeatherService weatherService;

    public FlightWeatherAdvisorService(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    public FlightWeatherAdvice analyzeForAirport(Airports airport) {
        WeatherSnapshot w = weatherService.getCurrentWeatherForAirport(airport);

        // --- Itt tudnád valójában OpenAI-hoz küldeni a promptot ---
        // pl. openAiClient.askWeatherAdvisor(w);
        // Én most egy egyszerű szabályrendszert használok:

        String status;
        String explanation;

        if (w.thunderstorm() || w.heavyRainOrSnow() || w.visibilityKm() < 1.0) {
            status = "DELAY";
            explanation = "Erős csapadék vagy zivatar, illetve nagyon rossz látási viszonyok várhatók. " +
                    "A felszállás halasztása erősen javasolt.";
        } else if (w.windSpeed() > 12.0 || w.windGust() > 18.0 || w.cloudCoverage() > 80) {
            status = "CAUTION";
            explanation = "Élénk vagy erős szél, illetve jelentős felhőzet várható. " +
                    "A felszállás lehetséges, de fokozott körültekintés és részletesebb döntéstámogatás szükséges.";
        } else {
            status = "GO";
            explanation = "Az időjárási feltételek általánosságban kedvezőek, " +
                    "nincs extrém szél vagy jelentős csapadék a következő órákban.";
        }

        explanation += String.format(
                "%nSzél: %.1f m/s (lökés: %.1f m/s), látótáv: %.1f km, felhőzet: %d%%, hőmérséklet: %.1f °C.",
                w.windSpeed(), w.windGust(), w.visibilityKm(), w.cloudCoverage(), w.temperature()
        );

        return new FlightWeatherAdvice(status, explanation);
    }
}