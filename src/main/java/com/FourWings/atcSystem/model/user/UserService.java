package com.FourWings.atcSystem.model.user;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

    @Transactional
    public void register(User u) {
        u.setPassword(passwordEncoder.encode(u.getPassword()));
        userRepository.save(u);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }
}
