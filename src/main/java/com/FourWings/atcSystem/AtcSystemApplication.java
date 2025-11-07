package com.FourWings.atcSystem;

import com.FourWings.atcSystem.model.user.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class AtcSystemApplication{

    @Autowired
    UserRepository userRepository;


    public static void main(String[] args) {
        SpringApplication.run(AtcSystemApplication.class, args);
    }

}
