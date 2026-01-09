package com.in4everyall.tennisclubmanager;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing(auditorAwareRef = "auditorAware")
public class TennisClubManagerApplication {

	public static void main(String[] args) {
		SpringApplication.run(TennisClubManagerApplication.class, args);
	}

}
