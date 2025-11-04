package com.FourWings.atcSystem;

import com.FourWings.atcSystem.model.User;
import com.FourWings.atcSystem.model.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.Scanner;

@SpringBootApplication
public class AtcSystemApplication implements CommandLineRunner {

    @Autowired
    UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        System.out.println("Kerlek a regisztraciohoz add meg az alabbi adataidat: ");
        Scanner scanner = new Scanner(System.in);

        System.out.print("Név: ");
        String name = scanner.nextLine();


        System.out.print("Felhasználónév: ");
        String username = scanner.nextLine();


        System.out.print("Email: ");
        String email = scanner.nextLine();


        System.out.print("Jelszó: ");
        String password = scanner.nextLine();

        System.out.print("Telefon: ");
        String phone = scanner.nextLine();

        User u =  User.builder()
                .name(name)
                .username(username)
                .email(email)
                .password(password)
                .phone(phone)
                .IsAdmin(false)
                .build();

        userRepository.save(u);
        scanner.close();
    }

    public static void main(String[] args) {
        SpringApplication.run(AtcSystemApplication.class, args);
    }

}
