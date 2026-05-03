package com.example.CafeAPP;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;
import org.springframework.scheduling.annotation.EnableAsync;

@EnableKafka
@SpringBootApplication
public class CafeAppApplication {

	public static void main(String[] args) {
		SpringApplication.run(CafeAppApplication.class, args);
	}
}
