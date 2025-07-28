// Hospital0515Application.java
package com.example.hospital_0515;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class Hospital0515Application {

	public static void main(String[] args) {
		SpringApplication.run(Hospital0515Application.class, args);
		System.out.println("http://localhost:8080");
	}
}
