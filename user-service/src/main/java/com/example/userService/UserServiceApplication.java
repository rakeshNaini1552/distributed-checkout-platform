package com.example.userService;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ApplicationContext;

@SpringBootApplication
public class UserServiceApplication {

	public static void main(String[] args) {
		System.out.println(new org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder().encode("test1234"));
		SpringApplication.run(UserServiceApplication.class, args);
	}

}
