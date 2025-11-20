package com.FourWings.atcSystem.model.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.util.List;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository,  PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public boolean isUsernameAvailable(String username) {
        return !userRepository.existsByUsername(username);
    }

    // 1) sima, önregisztráció (amit a RegistrationPage használ)
    @Transactional
    public User registerSelf(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        return userRepository.save(u);
    }

    /**
     * Admin által létrehozott / módosított felhasználó.
     *
     * rawPasswordOrNull:
     *  - ha NEM null/üres: új jelszó -> encode + mentés
     *  - ha null/üres: jelszó VÁLTOZATLAN marad a DB-ben
     *
     * FONTOS: meglévő usernél mindig a DB-s entitást töltjük be,
     * és CSAK a nem-jelszó mezőket írjuk felül a paraméterből.
     */
    @Transactional
    public User saveFromAdmin(User u, String rawPasswordOrNull) {

        if (u.getId() == 0) {
            // --- ÚJ USER ---
            // felhasználónév ütközés ellenőrzés
            if (userRepository.existsByUsername(u.getUsername())) {
                throw new IllegalStateException("A felhasználónév már foglalt: " + u.getUsername());
            }

            // ha van nyers jelszó paraméterben, azt encode-oljuk
            if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
                u.setPassword(passwordEncoder.encode(rawPasswordOrNull));
            } else if (u.getPassword() != null && !u.getPassword().isBlank()) {
                // opcionálisan: ha maga az entity tartalmaz raw jelszót (pl. registerSelf előtt),
                // akkor itt is encode-olhatnánk – de nálad új usernél eddig is mindig adtunk rawPasswordOrNull-t.
            }

            return userRepository.save(u);
        }

        // --- MEGLÉVŐ USER ---
        // username ütközés ellenőrzés (másik usernél van-e ugyanaz)
        boolean usernameTaken =
                userRepository.existsByUsernameAndIdNot(u.getUsername(), u.getId());
        if (usernameTaken) {
            throw new IllegalStateException("A felhasználónév már foglalt: " + u.getUsername());
        }

        // DB-ből frissen betöltött entitás (MANAGED)
        User dbUser = userRepository.findById(u.getId())
                .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található id=" + u.getId()));

        // CSAK az alábbi mezőket írjuk felül:
        dbUser.setName(u.getName());
        dbUser.setUsername(u.getUsername());
        dbUser.setEmail(u.getEmail());
        dbUser.setPhone(u.getPhone());
        dbUser.setAdmin(u.isAdmin());

        // A jelszó CSAK AKKOR változik, ha kaptunk nyers jelszót:
        if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
            dbUser.setPassword(passwordEncoder.encode(rawPasswordOrNull));
        }
        // Ha null/üres: érintetlenül hagyjuk dbUser.getPassword()-öt

        return userRepository.save(dbUser);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    public String generateTempPassword() {
        String chars = "ABCDEFGHJKLMNPQRSTUVWXYZabcdefghijkmnpqrstuvwxyz23456789!@#$%^&*";
        SecureRandom rnd = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 10; i++) {
            sb.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return sb.toString();
    }

    @Transactional
    public void deleteUserById(long id) {
        userRepository.deleteById(id);
    }

    @Transactional
    public void changePassword(long userId, String oldRawPassword, String newRawPassword) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található."));

        if (!passwordEncoder.matches(oldRawPassword, user.getPassword())) {
            throw new IllegalArgumentException("A régi jelszó nem helyes.");
        }

        if (newRawPassword == null || newRawPassword.length() < 8) {
            throw new IllegalArgumentException("Az új jelszó túl rövid (min. 8 karakter).");
        }

        String encoded = passwordEncoder.encode(newRawPassword);
        user.setPassword(encoded);
        userRepository.save(user);

        System.out.println("Jelszó módosítva userId=" + userId);
    }

    @Transactional
    public boolean changeOwnPassword(long userId, String oldRawPassword, String newRawPassword) {
        User dbUser = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Felhasználó nem található id=" + userId));

        if (!passwordEncoder.matches(oldRawPassword, dbUser.getPassword())) {
            return false;
        }

        dbUser.setPassword(passwordEncoder.encode(newRawPassword));
        userRepository.save(dbUser);
        return true;
    }
}