package com.FourWings.atcSystem;

import com.FourWings.atcSystem.frontend.MainPageController;
import com.FourWings.atcSystem.model.User;
import com.FourWings.atcSystem.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Scanner;

@SpringBootApplication
public class AtcSystemApplication{

    @Autowired
    UserRepository userRepository;


    public static void main(String[] args) {
        SpringApplication.run(AtcSystemApplication.class, args);
    }

}
