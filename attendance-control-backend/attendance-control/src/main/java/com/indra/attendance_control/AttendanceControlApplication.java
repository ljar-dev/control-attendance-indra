package com.indra.attendance_control;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.security.crypto.password.PasswordEncoder;

@SpringBootApplication
@EnableScheduling
public class AttendanceControlApplication implements CommandLineRunner{

	@Autowired
	private PasswordEncoder passwordEncoder;

	public static void main(String[] args) {
		SpringApplication.run(AttendanceControlApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		
		//genere contraseña con Bean PasswordEncoder
		String password = "12345";
		for (int i = 0; i < 2 ; i++) {
			String passwordEncoded = passwordEncoder.encode(password);
			System.out.println("Password encoded: " + passwordEncoded);
		}
	}
}
