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
}