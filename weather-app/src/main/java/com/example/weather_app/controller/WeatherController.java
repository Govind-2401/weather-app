package com.example.weatherapp.controller;

import com.example.weatherapp.dto.WeatherResponseDto;
import com.example.weatherapp.service.WeatherService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/weather")
@CrossOrigin(origins = "*")
@Tag(name = "Weather API", description = "Endpoints for fetching real-time weather and 5-day forecasts")
public class WeatherController {

    private final WeatherService weatherService;

    public WeatherController(WeatherService weatherService) {
        this.weatherService = weatherService;
    }

    @GetMapping("/{city}")
    @Operation(
            summary = "Get weather forecast by city name",
            description = "Fetches current temperature, humidity, wind, and daily 5-day forecast for the specified city."
    )
    public ResponseEntity<WeatherResponseDto> getWeather(@PathVariable String city) {
        return ResponseEntity.ok(weatherService.getWeatherForecast(city));
    }
}