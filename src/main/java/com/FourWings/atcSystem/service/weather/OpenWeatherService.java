package com.FourWings.atcSystem.service.weather;

import com.FourWings.atcSystem.model.airport.Airports;
import com.FourWings.atcSystem.service.WeatherService;
import com.FourWings.atcSystem.service.dto.AirportWeatherInfo;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Map;

@Service
public class OpenWeatherService implements WeatherService {

    @Value("${openweather.api.key}")
    private String apiKey;

    @Value("${openweather.api.baseurl:https://api.openweathermap.org/data/2.5}")
    private String baseUrl;

    private final RestTemplate restTemplate = new RestTemplate();

    /**
     * Ezt használja a Controller UI – formázott, szép szöveges adatokkal.
     */
    @Override
    public AirportWeatherInfo getCurrentWeatherForAirport(Airports airport) {
        WeatherSnapshot snapshot = getSnapshotForAirport(airport);

        // Emoji
        String emoji = "🌤️";
        if (snapshot.thunderstorm()) emoji = "⛈️";
        else if (snapshot.heavyRainOrSnow()) emoji = "🌧️";
        else if (snapshot.cloudCoverage() > 80) emoji = "☁️";
        else if (snapshot.cloudCoverage() > 40) emoji = "⛅";

        String conditionText;
        if (snapshot.thunderstorm()) {
            conditionText = "Zivatar, intenzív csapadék lehetséges";
        } else if (snapshot.heavyRainOrSnow()) {
            conditionText = "Erős eső vagy havazás";
        } else if (snapshot.cloudCoverage() > 80) {
            conditionText = "Borult égbolt";
        } else if (snapshot.cloudCoverage() > 40) {
            conditionText = "Változóan felhős";
        } else {
            conditionText = "Többnyire derült";
        }

        // Szél km/h-ba nagyjából, irány fok + iránybetű
        String windDir;
        int deg = snapshot.windDeg();
        if (deg >= 337 || deg < 23) windDir = "É";
        else if (deg < 68) windDir = "ÉK";
        else if (deg < 113) windDir = "K";
        else if (deg < 158) windDir = "DK";
        else if (deg < 203) windDir = "D";
        else if (deg < 248) windDir = "DNY";
        else if (deg < 293) windDir = "NY";
        else windDir = "ÉNY";

        double windKmH = snapshot.windSpeed() * 3.6;
        double gustKmH = snapshot.windGust() * 3.6;
        String windText = String.format("%d° (%s), %.0f km/h (lökés: %.0f km/h)",
                snapshot.windDeg(), windDir, windKmH, gustKmH);

        String visibilityText = String.format("%.1f km", snapshot.visibilityKm());
        String pressureText = String.format("%.0f hPa", snapshot.pressure());
        String feelsLikeText = String.format("Hőmérséklet: %.1f °C, páratartalom: %.0f%%",
                snapshot.temperature(), snapshot.humidity());

        String updatedAt = LocalDateTime.now()
                .format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));

        // METAR egyelőre nincs – később bővíthető
        String metarRaw = "METAR adatok nincsenek integrálva.";

        return new AirportWeatherInfo(
                emoji,
                snapshot.temperature(),
                conditionText,
                windText,
                visibilityText,
                pressureText,
                feelsLikeText,
                updatedAt,
                metarRaw
        );
    }

    /**
     * Ezt használja a FlightWeatherAdvisorService – nyers adatok, számolgatáshoz.
     */
    public WeatherSnapshot getSnapshotForAirport(Airports airport) {
        String city = airport.getCity();
        if (city == null || city.isBlank()) {
            throw new IllegalArgumentException("A repülőtérhez nincs város megadva.");
        }

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