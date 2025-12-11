# ATC System – Air Traffic Control Dashboard

A modern, JavaFX alapú **légiforgalmi irányító és reptéri dashboard rendszer**, Spring Boot háttérrel és MySQL adatbázissal.  
Célja, hogy egy gyakorlati, oktatási célú ATC rendszer prototípusát adja, ahol:

- repülőterek,
- légitársaságok,
- repülőgépek,
- járatok,
- terminálok és kapuk

kezelhetők, és különböző szerepkörű felhasználók (user, controller, admin) eltérő funkciókat érhetnek el.

---

## Fő funkciók

### 👤 Felhasználói szerepkörök

- **Normál felhasználó**
    - Saját adatok megtekintése / módosítása
    - Áttekintő dashboard a kiválasztott repülőtérről (járatok, időjárás, kapuk stb.)
- **Controller (irányító)**
    - Speciális **Controller dashboard**
    - Aznapi induló és érkező járatok áttekintése
    - Új járat létrehozása (home airport → destination airport)
    - Kapuk és terminálok figyelése (szabad kapuk száma)
    - Időjárás asszisztens megnyitása, részletes METAR/információk
- **Admin**
    - Admin felület (felhasználók, repterek, törzsadatok karbantartása – projekt lehetőségeitől függően)

---

## 🛫 Funkciók – részletesebben

### 1. Dashboard (User / Controller)

- **Időjárás kártya**
    - OpenWeather API-alapú lekérdezés
    - Hőmérséklet, szélirány és -sebesség, látótávolság, légnyomás, hőérzet
    - Emoji-alapú időjárás ikon (☀️ / ⛅ / 🌧️ / ⛈️)
    - Frissítés időpontja
    - METAR-szerű szöveges info (először csak placeholder, később bővíthető)

- **Mai járatok összefoglaló (Controller oldal)**
    - **Induló járatok (ma)** – aznapi scheduled departure count
    - **Érkező járatok (ma)** – aznapi scheduled arrival count
    - **Szabad kapuk** – Gate státusz + foglalt kapuk alapján számolt szabad kapuk száma

- **Induló / érkező járatok részletes listája**
    - Külön dialógusok:
        - `DeparturesDialog` – mai indulások
        - `ArrivalsDialog` – mai érkezések
    - Tábla nézet:
        - légitársaság, járatszám, célreptér / induló reptér
        - menetrend szerinti indulási / érkezési idő
        - becsült / tényleges idők
        - státusz (enum: `SCHEDULED`, `BOARDING`, `LANDED`, `DELAYED`, stb.)
    - Státusz és egyes időpontok **szerkeszthetők**, mentés után azonnali frissítés

- **Járat részletei dialógus**
    - A táblában kiválasztott járat részletes adatainak megtekintése
    - Nyitás: sor kijelölése + Enter (vagy dupla kattintás – implementációtól függően)

---

### 2. Új járat létrehozása

A `NewFlightDialog` segítségével a controller létrehozhat egy új járatot:

- Kiválasztható:
    - **Légitársaság** (home airport-hoz tartozó airlines)
    - **Célrepülőtér**
    - **Szabad repülőgép** (home airport-hoz rendelt és elérhető aircraft)
    - **Indulási / érkezési terminál** (home + destination airport termináljai)
    - **Indulási / érkezési kapu**

- Időpontok:
    - Indulási dátum + idő (DatePicker + TextField, pl. `14:30`)
    - Érkezési dátum + idő
    - Validáció: mindkét időpontnak érvényesnek kell lennie

- Mentés:
    - `Flight` entitás összeállítása:
        - departureAirport = homeAirport
        - arrivalAirport = destination
        - gate = indulási kapu
        - scheduledDeparture / scheduledArrival
        - estimatedDeparture / estimatedArrival (kezdetben megegyezhet a scheduled-del)
        - default státusz: `SCHEDULED`
    - Mentés `FlightService`-en keresztül

---

### 3. Időjárás integráció

- Szolgáltatás: `OpenWeatherService` (implementálja: `WeatherService`)
- Külső API: [OpenWeather](https://openweathermap.org/)
- Beállítás:
    - `openweather.api.key` – az API kulcs
    - `openweather.api.baseurl` – opcionális (default: `https://api.openweathermap.org/data/2.5`)
- Kimenet:
    - `AirportWeatherInfo` record (DTO)
    - formázott szövegek:
        - `conditionText`, `windText`, `visibilityText`, `pressureText`, `feelsLikeText`, `updatedAtText`, `metarRaw`

---

## 🧱 Technológiai stack

- **Java 17+ / 21+** (a projekt beállításaitól függően)
- **Spring Boot** – backend, service réteg, IoC konténer
- **JavaFX** – asztali GUI (FXML + Controller osztályok)
- **MySQL** – relációs adatbázis
- **JPA / Hibernate** – entitások és repository-k
- **Maven** – build és dependency kezelés

---

## 📁 Főbb csomagstruktúra

```text
com.FourWings.atcSystem
├─ config
│  ├─ SceneManager           # JavaFX scene váltás, full-screen kezelés
│  └─ SpringContext          # Spring bean-ek elérése FXML-ben
│
├─ frontend
│  ├─ MainPageController     # Login / remember-me logika
│  ├─ HomePageController     # User dashboard (időjárás + térkép + KPI)
│  ├─ FlightWeatherAssistantDialogController  # Időjárás segítség dialógus
│  └─ controller
│      ├─ ControllerHomePageController        # Controller dashboard
│      ├─ NewFlightDialogController           # Új járat dialógus
│      ├─ DeparturesDialogController          # Induló járatok dialógus
│      ├─ ArrivalsDialogController            # Érkező járatok dialógus
│      └─ WeatherDetailsDialogController      # Részletes időjárás
│
├─ model
│  ├─ airport   (Airports, AirportsService, AirportsRepository, ...)
│  ├─ airline   (Airline, AirlineService, ...)
│  ├─ aircraft  (Aircraft, AircraftService, ...)
│  ├─ flight    (Flight, FlightService, FlightStatus, ...)
│  ├─ gate      (Gate, GateService, ...)
│  ├─ terminal  (Terminal, TerminalService, ...)
│  └─ user      (User, UserRepository, szerepkörök: admin/controller/user)
│
└─ service
   ├─ WeatherService         # Absztrakció az időjárási szolgáltatásra
   ├─ weather/OpenWeatherService
   └─ dto/AirportWeatherInfo