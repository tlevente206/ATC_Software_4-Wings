package com.FourWings.atcSystem.service;

import com.FourWings.atcSystem.model.user.User;
import com.FourWings.atcSystem.model.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User loginAndGetUser(String username, String rawPassword) {
        var userOpt = userRepository.findByUsername(username);
        if (userOpt.isEmpty()) return null;

        User u = userOpt.get();
        if (passwordEncoder.matches(rawPassword, u.getPassword())) {
            return u;
        }
        return null;
    }
}
