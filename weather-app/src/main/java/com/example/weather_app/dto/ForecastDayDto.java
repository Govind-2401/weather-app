package com.example.weatherapp.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ForecastDayDto {
    private String date;
    private double minTemp;
    private double maxTemp;
    private int humidity;
    private String condition;
    private String icon;
}