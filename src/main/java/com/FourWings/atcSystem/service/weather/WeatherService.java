// src/main/java/com/FourWings/atcSystem/service/weather/WeatherService.java
package com.FourWings.atcSystem.service.weather;

import com.FourWings.atcSystem.model.airport.Airports;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;
import java.util.Map;

@Service
public class WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.baseurl:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    public WeatherSnapshot getCurrentWeatherForAirport(Airports airport) {
        // Várost az adatbázisból vesszük (pl. "Budapest")
        String city = airport.getCity(); // ha más a getter neve, írd át

        String url = baseUrl + "/weather?q=" + city + "&appid=" + apiKey + "&units=metric";

        Map<String, Object> json = restTemplate.getForObject(url, Map.class);
        if (json == null) {
            throw new IllegalStateException("Nem sikerült időjárási adatot lekérni: " + city);
        }

        Map<String, Object> wind   = (Map<String, Object>) json.get("wind");
        Map<String, Object> main   = (Map<String, Object>) json.get("main");
        Map<String, Object> clouds = (Map<String, Object>) json.get("clouds");
        List<Map<String, Object>> weather = (List<Map<String, Object>>) json.get("weather");

        double windSpeed = wind != null && wind.get("speed") != null ? ((Number) wind.get("speed")).doubleValue() : 0;
        double windGust  = wind != null && wind.get("gust")  != null ? ((Number) wind.get("gust")).doubleValue()  : 0;
        int windDeg      = wind != null && wind.get("deg")   != null ? ((Number) wind.get("deg")).intValue()      : 0;

        int visibility = json.get("visibility") != null ? ((Number) json.get("visibility")).intValue() : 0;
        int cloudCoverage = clouds != null && clouds.get("all") != null ? ((Number) clouds.get("all")).intValue() : 0;

        boolean thunderstorm = false;
        boolean heavyRainOrSnow = false;
        if (weather != null) {
            thunderstorm = weather.stream()
                    .anyMatch(w -> String.valueOf(w.get("main")).equalsIgnoreCase("Thunderstorm"));
            heavyRainOrSnow = weather.stream()
                    .anyMatch(w -> {
                        String mainW = String.valueOf(w.get("main")).toLowerCase();
                        String desc  = String.valueOf(w.get("description")).toLowerCase();
                        return desc.contains("heavy") || mainW.contains("snow");
                    });
        }

        double temp     = main != null && main.get("temp")     != null ? ((Number) main.get("temp")).doubleValue()     : 0;
        double pressure = main != null && main.get("pressure") != null ? ((Number) main.get("pressure")).doubleValue() : 0;
        double humidity = main != null && main.get("humidity") != null ? ((Number) main.get("humidity")).doubleValue() : 0;

        return new WeatherSnapshot(
                windSpeed,
                windGust,
                windDeg,
                visibility / 1000.0,
                cloudCoverage,
                thunderstorm,
                heavyRainOrSnow,
                temp,
                pressure,
                humidity
        );
    }
}