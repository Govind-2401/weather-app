package com.example.weatherapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

@SpringBootApplication
@EnableCaching
public class WeatherAppApplication {
	public static void main(String[] args) {
		SpringApplication.run(WeatherAppApplication.class, args);
	}
}