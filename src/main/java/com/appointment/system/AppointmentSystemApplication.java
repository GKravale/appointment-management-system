package com.appointment.system;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class AppointmentSystemApplication {

	public static void main(String[] args) {
		SpringApplication.run(AppointmentSystemApplication.class, args);
	}

}
