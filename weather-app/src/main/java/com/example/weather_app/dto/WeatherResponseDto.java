package com.example.weatherapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class WeatherResponseDto {
    private String city;
    private String country;
    private double currentTemperature;
    private double feelsLike;
    private int humidity;
    private double windSpeed;
    private String condition;
    private String icon;
    private List<ForecastDayDto> forecast;
}