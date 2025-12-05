package com.FourWings.atcSystem.model.user;

public enum UserRole {
    USER,        // sima felhasználó – csak nézelődik
    CONTROLLER,  // irányító – egy repülőtérhez kötve, járat, időjárás, stb.
    ADMIN        // teljes jog, minden táblát / usert kezel
}