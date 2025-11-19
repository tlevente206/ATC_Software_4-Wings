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

    // 2) admin által létrehozott / módosított felhasználó
    // rawPassword lehet:
    //   - null / üres: meglévő usernél nem változik a jelszó
    //   - konkrét jelszó: ezt titkosítjuk és mentjük
    @Transactional
    public User saveFromAdmin(User u, String rawPasswordOrNull) {
        // username ütközés ellenőrzés
        boolean usernameTaken;

        if (u.getId() == 0) {
            // új user
            usernameTaken = userRepository.existsByUsername(u.getUsername());
        } else {
            // meglévő user
            usernameTaken = userRepository.existsByUsernameAndIdNot(u.getUsername(), u.getId());
        }

        if (usernameTaken) {
            throw new IllegalStateException("A felhasználónév már foglalt: " + u.getUsername());
        }

        if (rawPasswordOrNull != null && !rawPasswordOrNull.isBlank()) {
            u.setPassword(passwordEncoder.encode(rawPasswordOrNull));
        }

        return userRepository.save(u);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    // 3) ideiglenes jelszó generálása – ezt a controller fogja hívni
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

    // UserService.java

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

        System.out.println("Jelszó módosítva userId=" + userId);  // debug
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